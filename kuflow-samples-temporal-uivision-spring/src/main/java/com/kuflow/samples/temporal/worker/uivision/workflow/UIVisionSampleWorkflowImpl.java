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

import com.kuflow.rest.model.Task;
import com.kuflow.rest.model.TaskDefinitionSummary;
import com.kuflow.temporal.activity.kuflow.KuFlowSyncActivities;
import com.kuflow.temporal.activity.kuflow.model.ClaimTaskRequest;
import com.kuflow.temporal.activity.kuflow.model.CompleteProcessRequest;
import com.kuflow.temporal.activity.kuflow.model.CompleteProcessResponse;
import com.kuflow.temporal.activity.kuflow.model.CompleteTaskRequest;
import com.kuflow.temporal.activity.kuflow.model.CreateTaskRequest;
import com.kuflow.temporal.activity.uivision.UIVisionActivities;
import com.kuflow.temporal.activity.uivision.model.ExecuteUIVisionMacroRequest;
import com.kuflow.temporal.common.KuFlowGenerator;
import com.kuflow.temporal.common.model.WorkflowRequest;
import com.kuflow.temporal.common.model.WorkflowResponse;
import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.workflow.Workflow;
import java.time.Duration;
import java.util.UUID;
import org.slf4j.Logger;

public class UIVisionSampleWorkflowImpl implements UIVisionSampleWorkflow {

    private static final Logger LOGGER = Workflow.getLogger(UIVisionSampleWorkflow.class);

    private static final String TASK_ROBOT_RESULTS = "ROBOT_RESULTS";

    private final KuFlowSyncActivities kuFlowSyncActivities;

    private final UIVisionActivities uiVisionActivities;

    private KuFlowGenerator kuflowGenerator;

    public UIVisionSampleWorkflowImpl() {
        RetryOptions defaultRetryOptions = RetryOptions.newBuilder().validateBuildWithDefaults();

        ActivityOptions defaultActivityOptions = ActivityOptions
            .newBuilder()
            .setRetryOptions(defaultRetryOptions)
            .setStartToCloseTimeout(Duration.ofMinutes(15))
            .setScheduleToCloseTimeout(Duration.ofDays(365))
            .validateAndBuildWithDefaults();

        this.kuFlowSyncActivities = Workflow.newActivityStub(KuFlowSyncActivities.class, defaultActivityOptions);

        this.uiVisionActivities = Workflow.newActivityStub(UIVisionActivities.class, defaultActivityOptions);
    }

    @Override
    public WorkflowResponse runWorkflow(WorkflowRequest workflowRequest) {
        this.kuflowGenerator = new KuFlowGenerator(workflowRequest.getProcessId());

        this.createTaskRobotResults(workflowRequest);

        CompleteProcessResponse completeProcess = this.completeProcess(workflowRequest);

        LOGGER.info("UiVision process finished. {}", workflowRequest.getProcessId());

        return this.completeWorkflow(completeProcess);
    }

    private WorkflowResponse completeWorkflow(CompleteProcessResponse completeProcess) {
        WorkflowResponse workflowResponse = new WorkflowResponse();
        workflowResponse.setMessage("Complete process " + completeProcess.getProcess().getId());

        return workflowResponse;
    }

    private CompleteProcessResponse completeProcess(WorkflowRequest workflowRequest) {
        CompleteProcessRequest request = new CompleteProcessRequest();
        request.setProcessId(workflowRequest.getProcessId());

        return this.kuFlowSyncActivities.completeProcess(request);
    }

    private void createTaskRobotResults(WorkflowRequest workflowRequest) {
        UUID taskId = this.kuflowGenerator.randomUUID();

        TaskDefinitionSummary tasksDefinition = new TaskDefinitionSummary();
        tasksDefinition.setCode(TASK_ROBOT_RESULTS);

        Task task = new Task();
        task.setId(taskId);
        task.setProcessId(workflowRequest.getProcessId());
        task.setTaskDefinition(tasksDefinition);

        CreateTaskRequest createTaskRequest = new CreateTaskRequest();
        createTaskRequest.setTask(task);
        this.kuFlowSyncActivities.createTask(createTaskRequest);

        ClaimTaskRequest claimTaskRequest = new ClaimTaskRequest();
        claimTaskRequest.setTaskId(taskId);
        this.kuFlowSyncActivities.claimTask(claimTaskRequest);

        ExecuteUIVisionMacroRequest executeUIVisionMacroRequest = new ExecuteUIVisionMacroRequest();
        executeUIVisionMacroRequest.setTaskId(taskId);
        this.uiVisionActivities.executeUIVisionMacro(executeUIVisionMacroRequest);

        CompleteTaskRequest completeTaskRequest = new CompleteTaskRequest();
        completeTaskRequest.setTaskId(taskId);
        this.kuFlowSyncActivities.completeTask(completeTaskRequest);
    }
}