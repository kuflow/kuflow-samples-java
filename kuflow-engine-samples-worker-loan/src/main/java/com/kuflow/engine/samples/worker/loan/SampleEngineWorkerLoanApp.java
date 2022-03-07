/*
 * Copyright (c) 2021-present KuFlow S.L.
 *
 * All rights reserved.
 */

package com.kuflow.engine.samples.worker.loan;

import com.kuflow.engine.client.activity.kuflow.config.KuFlowActivitiesConfiguration;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Optional;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

@SpringBootApplication
@EnableConfigurationProperties({ SampleEngineWorkerLoanProperties.class })
@Import({ KuFlowActivitiesConfiguration.class })
public class SampleEngineWorkerLoanApp implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(SampleEngineWorkerLoanApp.class);

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(SampleEngineWorkerLoanApp.class);
        Environment env = app.run(args).getEnvironment();
        logApplicationStartup(env);
    }

    @Override
    public void run(String... args) {
        LOGGER.info("Running...");
    }

    private static void logApplicationStartup(Environment env) {
        String protocol = Optional.ofNullable(env.getProperty("server.ssl.key-store")).map(key -> "https").orElse("http");
        String serverPort = env.getProperty("server.port");
        String contextPath = Optional
            .ofNullable(env.getProperty("server.servlet.context-path"))
            .filter(StringUtils::isNotBlank)
            .orElse("/");
        String hostAddress = "localhost";

        try {
            hostAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException ignore) {
            LOGGER.warn("The host name could not be determined, using `localhost` as fallback");
        }

        String[] profiles = ArrayUtils.isNotEmpty(env.getActiveProfiles()) ? env.getActiveProfiles() : env.getDefaultProfiles();

        LOGGER.info(
            """
            ----------------------------------------------------------
            \tApplication '{}' is running! Access URLs:
            \tLocal: \t\t{}://localhost:{}{}
            \tExternal: \t{}://{}:{}{}
            \tProfile(s): \t{}
            ----------------------------------------------------------
            """,
            env.getProperty("spring.application.name"),
            protocol,
            serverPort,
            contextPath,
            protocol,
            hostAddress,
            serverPort,
            contextPath,
            profiles
        );
    }
}
