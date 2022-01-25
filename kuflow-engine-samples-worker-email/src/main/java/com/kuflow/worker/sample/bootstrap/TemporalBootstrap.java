/*
 * Copyright (c) 2021-present KuFlow S.L.
 *
 * All rights reserved.
 */

package com.kuflow.worker.sample.bootstrap;

import com.kuflow.engine.client.activity.email.EmailActivities;
import com.kuflow.engine.client.activity.email.EmailActivitiesDelegate;
import com.kuflow.engine.client.activity.kuflow.KuFlowActivities;
import com.kuflow.engine.client.activity.kuflow.KuFlowActivitiesDelegate;
import com.kuflow.worker.sample.config.property.ApplicationProperties;
import com.kuflow.worker.sample.workflow.sample.SampleWorkflowImpl;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

@Component
public class TemporalBootstrap implements InitializingBean, DisposableBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(TemporalBootstrap.class);

    private final WorkerFactory factory;

    private final KuFlowActivities kuflowActivities;

    private final EmailActivities emailActivities;

    private final ApplicationProperties applicationProperties;

    public TemporalBootstrap(
        ApplicationProperties applicationProperties,
        WorkerFactory factory,
        KuFlowActivities kuflowActivities,
        EmailActivities emailActivities
    ) {
        this.applicationProperties = applicationProperties;
        this.factory = factory;
        this.kuflowActivities = kuflowActivities;
        this.emailActivities = emailActivities;
    }

    @Override
    public void afterPropertiesSet() {
        this.startWorkers();
        LOGGER.info("Temporal connection initialized");
    }

    @Override
    public void destroy() {
        this.factory.shutdown();
        this.factory.awaitTermination(1, TimeUnit.MINUTES);
        LOGGER.info("Temporal connection shutdown");
    }

    private void startWorkers() {
        Worker worker = this.factory.newWorker(this.applicationProperties.getTemporal().getKuflowQueue());
        worker.registerWorkflowImplementationTypes(SampleWorkflowImpl.class);
        worker.registerActivitiesImplementations(new KuFlowActivitiesDelegate(this.kuflowActivities));
        worker.registerActivitiesImplementations(new EmailActivitiesDelegate(this.emailActivities));

        this.factory.start();
    }
}
