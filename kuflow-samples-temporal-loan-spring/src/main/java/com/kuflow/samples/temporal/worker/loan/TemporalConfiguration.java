/*
 * The MIT License
 * Copyright © 2021-present KuFlow S.L.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.kuflow.samples.temporal.worker.loan;

import com.kuflow.rest.KuFlowRestClient;
import com.kuflow.samples.temporal.worker.loan.SampleEngineWorkerLoanProperties.TemporalProperties.MutualTlsProperties;
import com.kuflow.temporal.common.authorization.KuFlowAuthorizationTokenSupplier;
import com.kuflow.temporal.common.payload.codec.EncryptionPayloadCodec;
import com.kuflow.temporal.common.payload.codec.encryption.PayloadEncryptor;
import com.kuflow.temporal.common.payload.codec.encryption.PayloadEncryptors;
import com.kuflow.temporal.common.payload.codec.store.SecretStore;
import com.kuflow.temporal.common.payload.codec.store.SecretStores;
import com.kuflow.temporal.common.ssl.SslContextBuilder;
import com.kuflow.temporal.common.tracing.MDCContextPropagator;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import io.temporal.authorization.AuthorizationGrpcMetadataProvider;
import io.temporal.client.ActivityCompletionClient;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.common.converter.CodecDataConverter;
import io.temporal.common.converter.DefaultDataConverter;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import io.temporal.serviceclient.WorkflowServiceStubsOptions.Builder;
import io.temporal.worker.WorkerFactory;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class TemporalConfiguration {

    private final SampleEngineWorkerLoanProperties sampleEngineWorkerLoanProperties;

    private final KuFlowRestClient kuFlowRestClient;

    public TemporalConfiguration(SampleEngineWorkerLoanProperties SampleEngineWorkerLoanProperties, KuFlowRestClient kuFlowRestClient) {
        this.sampleEngineWorkerLoanProperties = SampleEngineWorkerLoanProperties;
        this.kuFlowRestClient = kuFlowRestClient;
    }

    @Bean(destroyMethod = "shutdown")
    public WorkflowServiceStubs workflowServiceStubs() {
        Builder builder = WorkflowServiceStubsOptions.newBuilder();
        builder.setTarget(this.sampleEngineWorkerLoanProperties.getTemporal().getTarget());
        builder.setSslContext(this.createSslContext());
        builder.addGrpcMetadataProvider(new AuthorizationGrpcMetadataProvider(new KuFlowAuthorizationTokenSupplier(this.kuFlowRestClient)));

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

        return SslContextBuilder
            .builder()
            .withCa(mutualTls.getCa())
            .withCaData(mutualTls.getCaData())
            .withCert(mutualTls.getCert())
            .withCertData(mutualTls.getCertData())
            .withKey(mutualTls.getKey())
            .withKeyData(mutualTls.getKeyData())
            .build();
    }
}
