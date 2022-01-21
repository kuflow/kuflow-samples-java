/*
 * Copyright (c) 2021-present KuFlow S.L.
 *
 * All rights reserved.
 */

package com.kuflow.worker.sample.workflow.sample;

import com.kuflow.engine.client.common.resource.WorkflowRequestResource;
import com.kuflow.engine.client.common.resource.WorkflowResponseResource;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface SampleWorkflow {
    public static final String WORKFLOW_NAME = SampleWorkflow.class.getSimpleName();

    @WorkflowMethod
    WorkflowResponseResource runWorkflow(WorkflowRequestResource request);
}
