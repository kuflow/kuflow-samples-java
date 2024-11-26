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
package com.kuflow.samples.temporal.worker.email.workflow;

import com.kuflow.rest.model.ProcessItem;
import com.kuflow.rest.model.ProcessItemTaskLogLevel;
import com.kuflow.rest.model.ProcessItemType;
import com.kuflow.temporal.activity.email.EmailActivities;
import com.kuflow.temporal.activity.email.model.Email;
import com.kuflow.temporal.activity.email.model.SendMailRequest;
import com.kuflow.temporal.activity.kuflow.KuFlowActivities;
import com.kuflow.temporal.activity.kuflow.model.ProcessItemCreateRequest;
import com.kuflow.temporal.activity.kuflow.model.ProcessItemRetrieveRequest;
import com.kuflow.temporal.activity.kuflow.model.ProcessItemRetrieveResponse;
import com.kuflow.temporal.activity.kuflow.model.ProcessItemTaskClaimRequest;
import com.kuflow.temporal.activity.kuflow.model.ProcessItemTaskCompleteRequest;
import com.kuflow.temporal.activity.kuflow.model.ProcessItemTaskLoggAppendRequest;
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
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class SampleWorkflowImpl implements SampleWorkflow {

    private enum TaskDefinitionCode {
        FILL_INFO,
        SEND_EMAIL,
    }

    private enum FormPropertyCode {
        EMAIL_RECIPIENT,
        EMAIL_SUBJECT,
        EMAIL_BODY,
    }

    private final KuFlowActivities kuFlowActivities;

    private final EmailActivities emailActivities;

    private final Set<UUID> kuFlowCompletedTaskIds = new HashSet<>();

    public SampleWorkflowImpl() {
        RetryOptions defaultRetryOptions = RetryOptions.newBuilder().validateBuildWithDefaults();

        ActivityOptions defaultActivityOptions = ActivityOptions.newBuilder()
            .setRetryOptions(defaultRetryOptions)
            .setStartToCloseTimeout(Duration.ofMinutes(10))
            .setScheduleToCloseTimeout(Duration.ofDays(365))
            .validateAndBuildWithDefaults();

        this.kuFlowActivities = Workflow.newActivityStub(KuFlowActivities.class, defaultActivityOptions);

        this.emailActivities = Workflow.newActivityStub(EmailActivities.class, defaultActivityOptions);
    }

    @Override
    public WorkflowResponse runWorkflow(WorkflowRequest workflowRequest) {
        UUID processId = workflowRequest.getProcessId();

        ProcessItem processItemFillInfo = this.createProcessItemFillInfo(processId);

        this.createAutomaticTaskSendEmail(processId, processItemFillInfo);

        return this.completeWorkflow(workflowRequest);
    }

    @Override
    public void handleKuFlowEngineSignalProcessItem(SignalProcessItem signal) {
        if (SignalProcessItemType.TASK.equals(signal.getType())) {
            this.kuFlowCompletedTaskIds.add(signal.getId());
        }
    }

    /**
     * Complete the Workflow
     * @param workflowRequest workflow request
     * @return the workflow response
     */
    private WorkflowResponse completeWorkflow(WorkflowRequest workflowRequest) {
        WorkflowResponse workflowResponse = new WorkflowResponse();
        workflowResponse.setMessage("Completed process " + workflowRequest.getProcessId());

        return workflowResponse;
    }

    /**
     * Create a process item task in KuFlow in order to collect the necessary information to send an email.
     *
     * @param processId process identifier
     * @return process item created
     */
    private ProcessItem createProcessItemFillInfo(UUID processId) {
        UUID processItemId = KuFlowWorkflow.generateUUIDv7();

        ProcessItemCreateRequest createRequest = new ProcessItemCreateRequest();
        createRequest.setId(processItemId);
        createRequest.setProcessId(processId);
        createRequest.setType(ProcessItemType.TASK);
        createRequest.setProcessItemDefinitionCode(TaskDefinitionCode.FILL_INFO.name());

        // Create Task in KuFlow
        this.createProcessItemAndWaitCompleted(createRequest);

        ProcessItemRetrieveRequest retrieveRequest = new ProcessItemRetrieveRequest();
        retrieveRequest.setProcessItemId(processItemId);
        ProcessItemRetrieveResponse retrieveResponse = this.kuFlowActivities.retrieveProcessItem(retrieveRequest);

        return retrieveResponse.getProcessItem();
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
     * @param infoProcessItem task with the data to send in the email
     */
    private void createAutomaticTaskSendEmail(UUID processId, ProcessItem infoProcessItem) {
        UUID processItemId = KuFlowWorkflow.generateUUIDv7();

        ProcessItemCreateRequest createRequest = new ProcessItemCreateRequest();
        createRequest.setId(processItemId);
        createRequest.setProcessId(processId);
        createRequest.setType(ProcessItemType.TASK);
        createRequest.setProcessItemDefinitionCode(TaskDefinitionCode.SEND_EMAIL.name());

        // Create Automatic Task in KuFlow
        this.createProcessItemAndWaitCompleted(createRequest);

        // Claim Automatic Task: Our worker will be responsible for its completion.
        ProcessItemTaskClaimRequest claimRequest = new ProcessItemTaskClaimRequest();
        claimRequest.setProcessItemId(processItemId);
        this.kuFlowActivities.claimProcessItemTask(claimRequest);

        Map<String, Object> infoProcessItemData = infoProcessItem.getTask().getData().getValue();

        // Get values from Info Task
        Email email = new Email();
        email.setTemplate("email");
        email.setTo(infoProcessItemData.get(FormPropertyCode.EMAIL_RECIPIENT.name()).toString());
        email.addVariables("subject", infoProcessItemData.get(FormPropertyCode.EMAIL_SUBJECT.name()).toString());
        email.addVariables("body", infoProcessItemData.get(FormPropertyCode.EMAIL_BODY.name()).toString());

        // Add some logs to Kuflow task in order to see feedback in Kuflow app
        this.addLogInfo(processItemId, "Sending email to " + email.getTo());

        // Send a mail
        SendMailRequest sendMailRequest = new SendMailRequest();
        sendMailRequest.setEmail(email);
        this.emailActivities.sendMail(sendMailRequest);

        // Add some logs to Kuflow task in order to see feedback in Kuflow app
        this.addLogInfo(processItemId, "Email sent!");

        // Activity Complete
        ProcessItemTaskCompleteRequest completeRequest = new ProcessItemTaskCompleteRequest();
        completeRequest.setProcessItemId(processItemId);
        this.kuFlowActivities.completeProcessItemTask(completeRequest);
    }

    /**
     * Add a log message to the referenced KuFlow task
     *
     * @param processItemId process item identifier
     * @param message a log message
     */
    private void addLogInfo(UUID processItemId, String message) {
        ProcessItemTaskLoggAppendRequest request = new ProcessItemTaskLoggAppendRequest();
        request.setProcessItemId(processItemId);
        request.setLevel(ProcessItemTaskLogLevel.INFO);
        request.setMessage(message);

        this.kuFlowActivities.appendProcessItemTaskLog(request);
    }

    /**
     * Create a task and wait for the task will be completed
     * @param request process item to create
     */
    private void createProcessItemAndWaitCompleted(ProcessItemCreateRequest request) {
        this.kuFlowActivities.createProcessItem(request);

        // Wait for completion
        Workflow.await(() -> this.kuFlowCompletedTaskIds.contains(request.getId()));
    }
}
