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

import com.kuflow.samples.temporal.worker.loan.activity.CurrencyConversionActivities;
import com.kuflow.samples.temporal.worker.loan.workflow.SampleEngineWorkerLoanWorkflowImpl;
import com.kuflow.temporal.activity.kuflow.KuFlowActivities;
import com.kuflow.temporal.worker.connection.KuFlowTemporalConnection;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

@Component
public class TemporalBootstrap implements InitializingBean, DisposableBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(TemporalBootstrap.class);

    private final KuFlowTemporalConnection kuFlowTemporalConnection;

    private final KuFlowActivities kuFlowActivities;

    private final CurrencyConversionActivities currencyConversionActivities;

    private final SampleEngineWorkerLoanProperties sampleEngineWorkerLoanProperties;

    public TemporalBootstrap(
        KuFlowTemporalConnection kuFlowTemporalConnection,
        KuFlowActivities kuFlowActivities,
        CurrencyConversionActivities currencyConversionActivities,
        SampleEngineWorkerLoanProperties sampleEngineWorkerLoanProperties
    ) {
        this.kuFlowTemporalConnection = kuFlowTemporalConnection;
        this.kuFlowActivities = kuFlowActivities;
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
        this.kuFlowTemporalConnection.shutdown(1, TimeUnit.MINUTES);
        LOGGER.info("Temporal connection shutdown");
    }

    private void startWorkers() {
        this.kuFlowTemporalConnection.configureWorker(builder ->
                builder
                    .withTaskQueue(this.sampleEngineWorkerLoanProperties.getTemporal().getKuflowQueue())
                    .withWorkflowImplementationTypes(SampleEngineWorkerLoanWorkflowImpl.class)
                    .withActivitiesImplementations(this.kuFlowActivities)
                    .withActivitiesImplementations(this.currencyConversionActivities)
            );

        this.kuFlowTemporalConnection.start();
    }
}
