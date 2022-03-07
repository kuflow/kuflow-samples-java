/*
 * Copyright (c) 2021-present KuFlow S.L.
 *
 * All rights reserved.
 */

package com.kuflow.engine.samples.worker.loan.workflow;

import com.kuflow.engine.client.common.resource.WorkflowRequestResource;
import com.kuflow.engine.client.common.resource.WorkflowResponseResource;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface SampleEngineWorkerLoanWorkflow {
    String WORKFLOW_NAME = SampleEngineWorkerLoanWorkflow.class.getSimpleName();

    @WorkflowMethod
    WorkflowResponseResource runWorkflow(WorkflowRequestResource request);
}
