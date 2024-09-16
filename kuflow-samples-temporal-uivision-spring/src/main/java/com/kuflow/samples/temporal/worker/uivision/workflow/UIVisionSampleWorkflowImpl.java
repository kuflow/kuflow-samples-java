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
package com.kuflow.samples.temporal.worker.uivision.workflow;

import com.kuflow.rest.model.ProcessItemTaskCreateParams;
import com.kuflow.rest.model.ProcessItemType;
import com.kuflow.temporal.activity.kuflow.KuFlowActivities;
import com.kuflow.temporal.activity.kuflow.model.ProcessItemCreateRequest;
import com.kuflow.temporal.activity.kuflow.model.ProcessItemTaskClaimRequest;
import com.kuflow.temporal.activity.kuflow.model.ProcessItemTaskCompleteRequest;
import com.kuflow.temporal.activity.uivision.UIVisionActivities;
import com.kuflow.temporal.activity.uivision.model.ExecuteUIVisionMacroRequest;
import com.kuflow.temporal.workflow.kuflow.KuFlowWorkflow;
import com.kuflow.temporal.workflow.kuflow.model.SignalProcessItem;
import com.kuflow.temporal.workflow.kuflow.model.SignalProcessItemType;
import com.kuflow.temporal.workflow.kuflow.model.WorkflowRequest;
import com.kuflow.temporal.workflow.kuflow.model.WorkflowResponse;
import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.workflow.Workflow;
import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;

public class UIVisionSampleWorkflowImpl implements UIVisionSampleWorkflow {

    private static final Logger LOGGER = Workflow.getLogger(UIVisionSampleWorkflow.class);

    private static final String TASK_ROBOT_RESULTS = "ROBOT_RESULTS";

    private final KuFlowActivities kuFlowActivities;

    private final UIVisionActivities uiVisionActivities;

    private final Set<UUID> kuFlowCompletedTaskIds = new HashSet<>();

    public UIVisionSampleWorkflowImpl() {
        RetryOptions defaultRetryOptions = RetryOptions.newBuilder().validateBuildWithDefaults();

        ActivityOptions defaultActivityOptions = ActivityOptions.newBuilder()
            .setRetryOptions(defaultRetryOptions)
            .setStartToCloseTimeout(Duration.ofMinutes(15))
            .setScheduleToCloseTimeout(Duration.ofDays(365))
            .validateAndBuildWithDefaults();

        this.kuFlowActivities = Workflow.newActivityStub(KuFlowActivities.class, defaultActivityOptions);

        this.uiVisionActivities = Workflow.newActivityStub(UIVisionActivities.class, defaultActivityOptions);
    }

    @Override
    public WorkflowResponse runWorkflow(WorkflowRequest workflowRequest) {
        this.createTaskRobotResults(workflowRequest);

        LOGGER.info("UiVision process finished. {}", workflowRequest.getProcessId());

        return this.completeWorkflow(workflowRequest);
    }

    @Override
    public void handleKuFlowEngineSignalProcessItem(SignalProcessItem signal) {
        if (SignalProcessItemType.TASK.equals(signal.getType())) {
            this.kuFlowCompletedTaskIds.add(signal.getId());
        }
    }

    private WorkflowResponse completeWorkflow(WorkflowRequest workflowRequest) {
        WorkflowResponse workflowResponse = new WorkflowResponse();
        workflowResponse.setMessage("Complete process " + workflowRequest.getPayloads());

        return workflowResponse;
    }

    private void createTaskRobotResults(WorkflowRequest workflowRequest) {
        UUID processItemId = KuFlowWorkflow.generateUUIDv7();

        // Create task in KuFlow
        ProcessItemTaskCreateParams createTaskRequest = new ProcessItemTaskCreateParams();
        createTaskRequest.setTaskDefinitionCode(TASK_ROBOT_RESULTS);

        ProcessItemCreateRequest createRequest = new ProcessItemCreateRequest();
        createRequest.setId(processItemId);
        createRequest.setType(ProcessItemType.TASK);
        createRequest.setProcessId(workflowRequest.getProcessId());
        createRequest.setTask(createTaskRequest);

        this.kuFlowActivities.createProcessItem(createRequest);

        // Claim task by the worker because is a valid candidate.
        // We could also claim it by specifying the "owner" in the above creation call.
        // We use the same application for the worker and for the robot.
        ProcessItemTaskClaimRequest claimRequest = new ProcessItemTaskClaimRequest();
        claimRequest.setProcessItemId(processItemId);
        this.kuFlowActivities.claimProcessItemTask(claimRequest);

        // Executes the Temporal activity to run the robot.
        ExecuteUIVisionMacroRequest executeUIVisionMacroRequest = new ExecuteUIVisionMacroRequest();
        executeUIVisionMacroRequest.setProcessItemId(processItemId);
        this.uiVisionActivities.executeUIVisionMacro(executeUIVisionMacroRequest);

        // Complete the task.
        ProcessItemTaskCompleteRequest completeRequest = new ProcessItemTaskCompleteRequest();
        completeRequest.setProcessItemId(processItemId);
        this.kuFlowActivities.completeProcessItemTask(completeRequest);
    }
}
