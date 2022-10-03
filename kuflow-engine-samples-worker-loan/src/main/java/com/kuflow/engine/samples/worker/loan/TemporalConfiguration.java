/*
 * Copyright (c) 2021-present KuFlow S.L.
 *
 * All rights reserved.
 */

package com.kuflow.engine.samples.worker.loan;

import com.kuflow.engine.client.common.authorization.KuFlowAuthorizationTokenSupplier;
import com.kuflow.engine.client.common.error.KuFlowEngineClientException;
import com.kuflow.engine.client.common.payload.codec.EncryptionPayloadCodec;
import com.kuflow.engine.client.common.payload.codec.encryption.PayloadEncryptor;
import com.kuflow.engine.client.common.payload.codec.encryption.PayloadEncryptors;
import com.kuflow.engine.client.common.payload.codec.store.SecretStore;
import com.kuflow.engine.client.common.payload.codec.store.SecretStores;
import com.kuflow.engine.client.common.tracing.MDCContextPropagator;
import com.kuflow.engine.samples.worker.loan.SampleEngineWorkerLoanProperties.TemporalProperties.MutualTlsProperties;
import com.kuflow.rest.client.controller.AuthenticationApi;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import io.temporal.authorization.AuthorizationGrpcMetadataProvider;
import io.temporal.client.ActivityCompletionClient;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.common.converter.CodecDataConverter;
import io.temporal.common.converter.DefaultDataConverter;
import io.temporal.serviceclient.SimpleSslContextBuilder;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import io.temporal.serviceclient.WorkflowServiceStubsOptions.Builder;
import io.temporal.worker.WorkerFactory;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class TemporalConfiguration {

    private final SampleEngineWorkerLoanProperties sampleEngineWorkerLoanProperties;

    private final AuthenticationApi authenticationApi;

    public TemporalConfiguration(SampleEngineWorkerLoanProperties SampleEngineWorkerLoanProperties, AuthenticationApi authenticationApi) {
        this.sampleEngineWorkerLoanProperties = SampleEngineWorkerLoanProperties;
        this.authenticationApi = authenticationApi;
    }

    @Bean(destroyMethod = "shutdown")
    public WorkflowServiceStubs workflowServiceStubs() {
        Builder builder = WorkflowServiceStubsOptions.newBuilder();
        builder.setTarget(this.sampleEngineWorkerLoanProperties.getTemporal().getTarget());
        builder.setSslContext(this.createSslContext());
        builder.addGrpcMetadataProvider(
            new AuthorizationGrpcMetadataProvider(new KuFlowAuthorizationTokenSupplier(this.authenticationApi))
        );

        WorkflowServiceStubsOptions options = builder.validateAndBuildWithDefaults();

        return WorkflowServiceStubs.newServiceStubs(options);
    }

    @Bean
    public EncryptionPayloadCodec encryptionPayloadCodec() {
        String defaultSecretKeyId = "test-key-test-key-test-key-test!";
        SecretKey secretKey = new SecretKeySpec(defaultSecretKeyId.getBytes(StandardCharsets.UTF_8), "AES");

        SecretStore secretStore = SecretStores.memory(defaultSecretKeyId, Map.of(defaultSecretKeyId, secretKey));
        PayloadEncryptor payloadEncryptor = PayloadEncryptors.aesGcm(secretStore);

        return new EncryptionPayloadCodec(payloadEncryptor);
    }

    @Bean
    public WorkflowClient workflowClient(WorkflowServiceStubs service, EncryptionPayloadCodec encryptionPayloadCodec) {
        WorkflowClientOptions options = WorkflowClientOptions
            .newBuilder()
            .setNamespace(this.sampleEngineWorkerLoanProperties.getTemporal().getNamespace())
            .setContextPropagators(List.of(new MDCContextPropagator()))
            .setDataConverter(new CodecDataConverter(DefaultDataConverter.newDefaultInstance(), List.of(encryptionPayloadCodec)))
            .build();

        return WorkflowClient.newInstance(service, options);
    }

    @Bean
    public WorkerFactory workerFactory(WorkflowClient workflowClient) {
        return WorkerFactory.newInstance(workflowClient);
    }

    @Bean
    public ActivityCompletionClient activityCompletionClient(WorkflowClient workflowClient) {
        return workflowClient.newActivityCompletionClient();
    }

    private SslContext createSslContext() {
        MutualTlsProperties mutualTls = this.sampleEngineWorkerLoanProperties.getTemporal().getMutualTls();
        if (StringUtils.isBlank(mutualTls.getCert()) && StringUtils.isBlank(mutualTls.getCertData())) {
            return null;
        }

        if (
            StringUtils.isNotBlank(mutualTls.getCert()) &&
            (StringUtils.isBlank(mutualTls.getKey()) || StringUtils.isBlank(mutualTls.getCa()))
        ) {
            throw new KuFlowEngineClientException("key and ca are required");
        }

        if (
            StringUtils.isNotBlank(mutualTls.getCertData()) &&
            (StringUtils.isBlank(mutualTls.getKeyData()) || StringUtils.isBlank(mutualTls.getCaData()))
        ) {
            throw new KuFlowEngineClientException("keyData or caData are required");
        }

        try (
            InputStream certInputStream = this.openInputStream(mutualTls.getCert(), mutualTls.getCertData());
            InputStream keyInputStream = this.openInputStream(mutualTls.getKey(), mutualTls.getKeyData());
            InputStream caInputStream = this.openInputStream(mutualTls.getCa(), mutualTls.getCaData())
        ) {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);
            X509Certificate certificate = (X509Certificate) CertificateFactory.getInstance("X509").generateCertificate(caInputStream);
            trustStore.setCertificateEntry("temporal-ca", certificate);

            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(trustStore);
            TrustManager trustManager = trustManagerFactory.getTrustManagers()[0];

            return SimpleSslContextBuilder.forPKCS8(certInputStream, keyInputStream).setTrustManager(trustManager).build();
        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
            throw new KuFlowEngineClientException("Unable to configure mTLS", e);
        }
    }

    private InputStream openInputStream(String file, String pem) {
        if (StringUtils.isNotBlank(file)) {
            return this.openInputStreamFromFile(file);
        }

        return this.openInputStreamFromPem(pem);
    }

    private InputStream openInputStreamFromFile(String file) {
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new KuFlowEngineClientException(String.format("Unable to load %s", file));
        }
    }

    private InputStream openInputStreamFromPem(String pem) {
        return new ByteArrayInputStream(pem.getBytes(StandardCharsets.UTF_8));
    }
}
