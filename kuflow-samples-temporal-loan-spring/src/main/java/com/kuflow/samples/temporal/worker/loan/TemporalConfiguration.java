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
import com.kuflow.samples.temporal.worker.loan.SampleEngineWorkerLoanProperties.TemporalProperties;
import com.kuflow.temporal.common.connection.KuFlowTemporalConnection;
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

    @Bean
    public KuFlowTemporalConnection kuFlowTemporalConnection() {
        TemporalProperties temporalProperties = this.sampleEngineWorkerLoanProperties.getTemporal();

        return KuFlowTemporalConnection.instance(this.kuFlowRestClient).configureWorkflowServiceStubs(
            builder -> builder.setTarget(temporalProperties.getTarget())
        );
    }
}
