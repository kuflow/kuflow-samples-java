/*
 * Copyright (c) 2021-present KuFlow S.L.
 *
 * All rights reserved.
 */

package com.kuflow.worker.sample;

import com.kuflow.engine.client.activity.impl.email.config.KuFlowActivityEmailConfiguration;
import com.kuflow.engine.client.activity.impl.task.config.KuFlowActivityTaskConfiguration;
import com.kuflow.worker.sample.config.property.ApplicationProperties;
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
@EnableConfigurationProperties({ ApplicationProperties.class })
@Import({ KuFlowActivityTaskConfiguration.class, KuFlowActivityEmailConfiguration.class })
public class SampleWorkerApp implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(SampleWorkerApp.class);

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(SampleWorkerApp.class);
        Environment env = app.run(args).getEnvironment();
        logApplicationStartup(env);
    }

    @Override
    public void run(String... args) throws Exception {
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
            "\n----------------------------------------------------------" +
            "\n\tApplication '{}' is running! Access URLs:" +
            "\n\tLocal: \t\t{}://localhost:{}{}" +
            "\n\tExternal: \t{}://{}:{}{}" +
            "\n\tProfile(s): \t{}" +
            "\n----------------------------------------------------------",
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
