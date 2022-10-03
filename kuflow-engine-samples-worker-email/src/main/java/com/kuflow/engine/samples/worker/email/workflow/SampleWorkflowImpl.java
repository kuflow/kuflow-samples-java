/*
 * Copyright (c) 2021-present KuFlow S.L.
 *
 * All rights reserved.
 */

package com.kuflow.engine.samples.worker.email.workflow;

import com.kuflow.engine.client.activity.email.EmailActivities;
import com.kuflow.engine.client.activity.email.resource.EmailResource;
import com.kuflow.engine.client.activity.email.resource.SendMailRequestResource;
import com.kuflow.engine.client.activity.kuflow.KuFlowActivities;
import com.kuflow.engine.client.activity.kuflow.resource.CompleteProcessRequestResource;
import com.kuflow.engine.client.activity.kuflow.resource.CompleteProcessResponseResource;
import com.kuflow.engine.client.activity.kuflow.resource.CreateTaskRequestResource;
import com.kuflow.engine.client.activity.kuflow.resource.CreateTaskResponseResource;
import com.kuflow.engine.client.activity.kuflow.resource.LogRequestResource;
import com.kuflow.engine.client.activity.kuflow.resource.RetrieveTaskRequestResource;
import com.kuflow.engine.client.activity.kuflow.resource.RetrieveTaskResponseResource;
import com.kuflow.engine.client.activity.kuflow.resource.TaskClaimRequestResource;
import com.kuflow.engine.client.activity.kuflow.resource.TaskCompleteRequestResource;
import com.kuflow.engine.client.common.resource.WorkflowRequestResource;
import com.kuflow.engine.client.common.resource.WorkflowResponseResource;
import com.kuflow.engine.client.common.util.TemporalUtils;
import com.kuflow.rest.client.resource.LogLevelResource;
import com.kuflow.rest.client.resource.TaskElementValueWrapperResource;
import com.kuflow.rest.client.resource.TaskResource;
import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.workflow.Workflow;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;

public class SampleWorkflowImpl implements SampleWorkflow {

    private enum TaskDefinitionCode {
        FILL_INFO,
        SEND_EMAIL,
    }

    private enum ElementDefinitionCode {
        EMAIL_RECIPIENT,
        EMAIL_SUBJECT,
        EMAIL_BODY,
    }

    private final KuFlowActivities kuflowActivities;

    private final EmailActivities emailActivities;

    public SampleWorkflowImpl() {
        RetryOptions defaultRetryOptions = RetryOptions.newBuilder().validateBuildWithDefaults();

        ActivityOptions defaultActivityOptions = ActivityOptions
            .newBuilder()
            .setRetryOptions(defaultRetryOptions)
            .setStartToCloseTimeout(Duration.ofMinutes(10))
            .setScheduleToCloseTimeout(Duration.ofDays(365))
            .validateAndBuildWithDefaults();

        ActivityOptions asyncActivityOptions = ActivityOptions
            .newBuilder()
            .setRetryOptions(defaultRetryOptions)
            .setStartToCloseTimeout(Duration.ofDays(1))
            .setScheduleToCloseTimeout(Duration.ofDays(365))
            .validateAndBuildWithDefaults();

        this.kuflowActivities =
            Workflow.newActivityStub(
                KuFlowActivities.class,
                defaultActivityOptions,
                Map.of(TemporalUtils.getActivityType(KuFlowActivities.class, "createTaskAndWaitFinished"), asyncActivityOptions)
            );

        this.emailActivities = Workflow.newActivityStub(EmailActivities.class, defaultActivityOptions);
    }

    @Override
    public WorkflowResponseResource runWorkflow(WorkflowRequestResource workflowRequest) {
        UUID processId = workflowRequest.getProcessId();

        TaskResource taskFillInfo = this.createTaskFillInfo(processId);

        this.createAutomaticTaskSendEmail(processId, taskFillInfo);

        String completeProcessMessage = this.completeProcess(processId);

        return this.completeWorkflow(completeProcessMessage);
    }

    /**
     * Complete the Workflow
     * @param completeProcessMessage message
     * @return the workflow response
     */
    private WorkflowResponseResource completeWorkflow(String completeProcessMessage) {
        WorkflowResponseResource workflowResponse = new WorkflowResponseResource();
        workflowResponse.setMessage(completeProcessMessage);

        return workflowResponse;
    }

    /**
     * Complete the process
     *
     * @param processId process identifier
     * @return response message
     */
    private String completeProcess(UUID processId) {
        CompleteProcessRequestResource request = new CompleteProcessRequestResource();
        request.setProcessId(processId);

        CompleteProcessResponseResource response = this.kuflowActivities.completeProcess(request);

        return response.getMessage();
    }

    /**
     * Create a task in KuFlow in order to collect the necessary information to send an email.
     *
     * @param processId process identifier
     * @return task created
     */
    private TaskResource createTaskFillInfo(UUID processId) {
        UUID taskId = UUID.randomUUID();

        CreateTaskRequestResource createTaskRequest = new CreateTaskRequestResource();
        createTaskRequest.setTaskId(taskId);
        createTaskRequest.setProcessId(processId);
        createTaskRequest.setTaskDefinitionCode(TaskDefinitionCode.FILL_INFO.name());

        // Create Task in KuFlow
        this.kuflowActivities.createTaskAndWaitFinished(createTaskRequest);

        RetrieveTaskRequestResource retrieveTaskRequest = new RetrieveTaskRequestResource();
        retrieveTaskRequest.setTaskId(taskId);
        RetrieveTaskResponseResource retrieveTaskResponse = this.kuflowActivities.retrieveTask(retrieveTaskRequest);

        return retrieveTaskResponse.getTask();
    }

    /**
     * Execute a Temporal activity that sends an email with the data from a previous KuFlow task.
     * <br>
     * To see the activity process reflected in the KuFlow application, we created a task.
     * The execution of Temporal activities does not have to have direct correspondence with KuFlow tasks. Its use
     * depends on your Workflow logic. In the same way, several activities could be executed and have a single
     * automatic task in Kuflow that encompasses them. As always, it all depends on the requirements of your workflow.
     *
     * @param processId process identifier
     * @param infoTask task with the data to send in the email
     * @return task created
     */
    private CreateTaskResponseResource createAutomaticTaskSendEmail(UUID processId, TaskResource infoTask) {
        CreateTaskRequestResource task = new CreateTaskRequestResource();
        task.setTaskId(Workflow.randomUUID());
        task.setProcessId(processId);
        task.setTaskDefinitionCode(TaskDefinitionCode.SEND_EMAIL.name());

        // Create Automatic Task in KuFlow
        CreateTaskResponseResource taskResponse = this.kuflowActivities.createTask(task);

        // Claim Automatic Task: Our worker will be responsible for its completion.
        TaskClaimRequestResource taskClaimrequestResource = new TaskClaimRequestResource();
        taskClaimrequestResource.setTaskId(taskResponse.getTask().getId());
        this.kuflowActivities.claimTask(taskClaimrequestResource);

        // Get values from Info Task
        Map<String, TaskElementValueWrapperResource> infoTaskElementValues = infoTask.getElementValues();

        EmailResource email = new EmailResource();
        email.setTemplate("email");
        email.setTo(infoTaskElementValues.get(ElementDefinitionCode.EMAIL_RECIPIENT.name()).getValueAsString());
        email.addVariables("subject", infoTaskElementValues.get(ElementDefinitionCode.EMAIL_SUBJECT.name()).getValueAsString());
        email.addVariables("body", infoTaskElementValues.get(ElementDefinitionCode.EMAIL_BODY.name()).getValueAsString());

        // Send a mail
        SendMailRequestResource request = new SendMailRequestResource();
        request.setEmail(email);
        this.emailActivities.sendMail(request);

        // Add some logs to Kuflow task in order to see feedback in Kuflow app
        this.addLogInfoEntryTask(taskResponse.getTask().getId(), "Email sent!");

        // Activity Complete
        TaskCompleteRequestResource completeRequest = new TaskCompleteRequestResource();
        completeRequest.setTaskId(taskResponse.getTask().getId());
        this.kuflowActivities.completeTask(completeRequest);

        return taskResponse;
    }

    /**
     * Add a log message to the referenced KuFlow task
     *
     * @param taskId task identifier
     * @param message a log message
     */
    private void addLogInfoEntryTask(UUID taskId, String message) {
        LogRequestResource request = new LogRequestResource();
        request.setTaskId(taskId);
        request.setLogId(Workflow.randomUUID());
        request.setLevel(LogLevelResource.INFO);
        request.setMessage(message);

        this.kuflowActivities.appendTaskLog(request);
    }
}
