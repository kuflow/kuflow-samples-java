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

import static java.util.Collections.enumeration;
import static java.util.stream.Collectors.joining;

import com.kuflow.rest.KuFlowRestClient;
import com.kuflow.rest.KuFlowRestClientBuilder;
import com.kuflow.samples.temporal.worker.loan.SampleEngineWorkerLoanProperties.KuFlowApiProperties;
import com.kuflow.samples.temporal.worker.loan.activity.CurrencyConversionActivities;
import com.kuflow.samples.temporal.worker.loan.activity.CurrencyConversionActivitiesImpl;
import com.kuflow.samples.temporal.worker.loan.common.BearerAuthenticationCredential;
import com.kuflow.samples.temporal.worker.loan.workflow.SampleEngineWorkerLoanWorkflowImpl;
import com.kuflow.temporal.activity.kuflow.KuFlowActivities;
import com.kuflow.temporal.activity.kuflow.KuFlowActivitiesImpl;
import com.kuflow.temporal.common.connection.KuFlowTemporalConnection;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
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

        KuFlowApiProperties apiProperties = properties.getKuflow().getApi();
        KuFlowRestClient kuFlowRestClient = new KuFlowRestClientBuilder()
            .clientId(apiProperties.getClientId())
            .clientSecret(apiProperties.getClientSecret())
            .credential(apiProperties.getToken() != null ? new BearerAuthenticationCredential(apiProperties.getToken()) : null)
            .endpoint(apiProperties.getEndpoint())
            .allowInsecureConnection(apiProperties.getEndpoint() != null && apiProperties.getEndpoint().startsWith("http://"))
            .buildClient();

        KuFlowTemporalConnection kuFlowTemporalConnection = KuFlowTemporalConnection.instance(kuFlowRestClient)
            .withInstallationId(properties.getTemporal().getInstallationId())
            .withTenantId(properties.getTemporal().getTenantId())
            .withRobotIds(properties.getTemporal().getRobotIds())
            .configureWorkflowServiceStubs(builder -> builder.setTarget(properties.getTemporal().getTarget()))
            .configureWorker(builder -> {
                KuFlowActivities kuFlowActivities = new KuFlowActivitiesImpl(kuFlowRestClient);
                CurrencyConversionActivities conversionActivities = new CurrencyConversionActivitiesImpl();

                builder
                    .withTaskQueue(properties.getTemporal().getKuflowQueue())
                    .withWorkflowImplementationTypes(SampleEngineWorkerLoanWorkflowImpl.class)
                    .withActivitiesImplementations(kuFlowActivities)
                    .withActivitiesImplementations(conversionActivities);
            });

        kuFlowTemporalConnection.start();

        LOGGER.info(
            """
            \n----------------------------------------------------------
            \tApplication 'SampleEngineWorkerLoan' is running!
            ----------------------------------------------------------
            """
        );
        Runtime.getRuntime()
            .addShutdownHook(
                new Thread(() -> {
                    kuFlowTemporalConnection.shutdown(1, TimeUnit.MINUTES);
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
                    name = Arrays.stream(name.split("-"))
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
}
