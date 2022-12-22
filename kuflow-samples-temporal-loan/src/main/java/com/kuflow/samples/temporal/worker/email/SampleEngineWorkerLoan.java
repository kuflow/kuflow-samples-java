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
package com.kuflow.samples.temporal.worker.email;

import static java.util.Collections.enumeration;
import static java.util.stream.Collectors.joining;

import com.kuflow.rest.KuFlowRestClient;
import com.kuflow.rest.KuFlowRestClientBuilder;
import com.kuflow.samples.temporal.worker.email.SampleEngineWorkerLoanProperties.TemporalProperties.MutualTlsProperties;
import com.kuflow.samples.temporal.worker.email.activity.CurrencyConversionActivities;
import com.kuflow.samples.temporal.worker.email.activity.CurrencyConversionActivitiesImpl;
import com.kuflow.samples.temporal.worker.email.workflow.SampleEngineWorkerLoanWorkflowImpl;
import com.kuflow.temporal.activity.kuflow.KuFlowAsyncActivities;
import com.kuflow.temporal.activity.kuflow.KuFlowAsyncActivitiesImpl;
import com.kuflow.temporal.activity.kuflow.KuFlowSyncActivities;
import com.kuflow.temporal.activity.kuflow.KuFlowSyncActivitiesImpl;
import com.kuflow.temporal.common.authorization.KuFlowAuthorizationTokenSupplier;
import com.kuflow.temporal.common.ssl.SslContextBuilder;
import com.kuflow.temporal.common.tracing.MDCContextPropagator;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import io.temporal.authorization.AuthorizationGrpcMetadataProvider;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.introspector.PropertyUtils;

public class SampleEngineWorkerLoan {

    private static final Logger LOGGER = LoggerFactory.getLogger(SampleEngineWorkerLoan.class);

    public static void main(String[] args) {
        SampleEngineWorkerLoanProperties properties = loadConfiguration();

        KuFlowRestClient kuFlowRestClient = kuFlowRestClient(properties);

        WorkflowServiceStubs service = workflowServiceStubs(properties, kuFlowRestClient);

        WorkflowClient client = workflowClient(properties, service);

        WorkerFactory factory = WorkerFactory.newInstance(client);

        KuFlowSyncActivities kuFlowSyncActivities = new KuFlowSyncActivitiesImpl(kuFlowRestClient);
        KuFlowAsyncActivities kuFlowAsyncActivities = new KuFlowAsyncActivitiesImpl(kuFlowRestClient);
        CurrencyConversionActivities conversionActivities = new CurrencyConversionActivitiesImpl();

        Worker worker = factory.newWorker(properties.getTemporal().getKuflowQueue());
        worker.registerWorkflowImplementationTypes(SampleEngineWorkerLoanWorkflowImpl.class);
        worker.registerActivitiesImplementations(kuFlowSyncActivities);
        worker.registerActivitiesImplementations(kuFlowAsyncActivities);
        worker.registerActivitiesImplementations(conversionActivities);

        factory.start();

        LOGGER.info(
            """

            ----------------------------------------------------------
            \tApplication 'SampleEngineWorkerLoan' is running!
            ----------------------------------------------------------
            """
        );
        Runtime
            .getRuntime()
            .addShutdownHook(
                new Thread(() -> {
                    factory.shutdown();
                    LOGGER.info("Shutting down ...");
                })
            );
    }

    private static SampleEngineWorkerLoanProperties loadConfiguration() {
        Constructor constructor = new Constructor(SampleEngineWorkerLoanProperties.class);
        constructor.setPropertyUtils(
            new PropertyUtils() {
                @Override
                public Property getProperty(Class<?> type, String name) {
                    name =
                        Arrays
                            .stream(name.split("-"))
                            .map(s -> Character.toUpperCase(s.charAt(0)) + s.substring(1).toLowerCase())
                            .collect(joining());
                    name = Character.toLowerCase(name.charAt(0)) + name.substring(1);
                    return super.getProperty(type, name);
                }
            }
        );

        Yaml yaml = new Yaml(constructor);

        List<InputStream> streams = new LinkedList<>();

        loadConfigurationFile("/config/application.yaml", streams);
        loadConfigurationFile("/config/application-local.yaml", streams);
        loadConfigurationFile("/config/application-local-sandbox.yaml", streams);

        try (InputStream inputStream = new SequenceInputStream(enumeration(streams))) {
            return yaml.load(inputStream);
        } catch (IOException e) {
            LOGGER.error("Error reading configurations", e);
            throw new RuntimeException(e);
        }
    }

    private static void loadConfigurationFile(String name, List<InputStream> streams) {
        InputStream inputApplication = SampleEngineWorkerLoan.class.getResourceAsStream(name);
        if (inputApplication != null) {
            LOGGER.info("Loading configuration file {}", name);
            streams.add(inputApplication);
        } else {
            LOGGER.warn("Configuration file {} not found", name);
        }
    }

    private static KuFlowRestClient kuFlowRestClient(SampleEngineWorkerLoanProperties properties) {
        KuFlowRestClientBuilder builder = new KuFlowRestClientBuilder();
        builder.clientId(properties.getKuflow().getApi().getClientId());
        builder.clientSecret(properties.getKuflow().getApi().getClientSecret());
        String endpoint = properties.getKuflow().getApi().getEndpoint();
        if (endpoint != null) {
            builder.endpoint(endpoint);
            builder.allowInsecureConnection(endpoint.startsWith("http://"));
        }

        return builder.buildClient();
    }

    public static WorkflowServiceStubs workflowServiceStubs(
        SampleEngineWorkerLoanProperties properties,
        KuFlowRestClient kuFlowRestClient
    ) {
        WorkflowServiceStubsOptions.Builder builder = WorkflowServiceStubsOptions.newBuilder();
        builder.setTarget(properties.getTemporal().getTarget());
        builder.setSslContext(createSslContext(properties));
        builder.addGrpcMetadataProvider(new AuthorizationGrpcMetadataProvider(new KuFlowAuthorizationTokenSupplier(kuFlowRestClient)));

        WorkflowServiceStubsOptions options = builder.validateAndBuildWithDefaults();

        return WorkflowServiceStubs.newServiceStubs(options);
    }

    public static WorkflowClient workflowClient(SampleEngineWorkerLoanProperties properties, WorkflowServiceStubs service) {
        WorkflowClientOptions options = WorkflowClientOptions
            .newBuilder()
            .setNamespace(properties.getTemporal().getNamespace())
            .setContextPropagators(List.of(new MDCContextPropagator()))
            .build();

        return WorkflowClient.newInstance(service, options);
    }

    private static SslContext createSslContext(SampleEngineWorkerLoanProperties properties) {
        MutualTlsProperties mutualTls = properties.getTemporal().getMutualTls();

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
