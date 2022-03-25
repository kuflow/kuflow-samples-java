/*
 * Copyright (c) 2021-present KuFlow S.L.
 *
 * All rights reserved.
 */

package com.kuflow.engine.samples.worker.loan;

import com.kuflow.engine.client.activity.kuflow.KuFlowActivities;
import com.kuflow.engine.samples.worker.loan.activity.CurrencyConversionActivities;
import com.kuflow.engine.samples.worker.loan.workflow.SampleEngineWorkerLoanWorkflowImpl;
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

    private final CurrencyConversionActivities currencyConversionActivities;

    private final SampleEngineWorkerLoanProperties sampleEngineWorkerLoanProperties;

    public TemporalBootstrap(
        WorkerFactory factory,
        KuFlowActivities kuflowActivities,
        CurrencyConversionActivities currencyConversionActivities,
        SampleEngineWorkerLoanProperties sampleEngineWorkerLoanProperties
    ) {
        this.factory = factory;
        this.kuflowActivities = kuflowActivities;
        this.currencyConversionActivities = currencyConversionActivities;
        this.sampleEngineWorkerLoanProperties = sampleEngineWorkerLoanProperties;
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
        Worker worker = this.factory.newWorker(this.sampleEngineWorkerLoanProperties.getTemporal().getKuflowQueue());
        worker.registerWorkflowImplementationTypes(SampleEngineWorkerLoanWorkflowImpl.class);
        worker.registerActivitiesImplementations(this.kuflowActivities);
        worker.registerActivitiesImplementations(this.currencyConversionActivities);

        this.factory.start();
    }
}
