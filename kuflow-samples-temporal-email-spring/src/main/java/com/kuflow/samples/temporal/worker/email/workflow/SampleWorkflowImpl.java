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

import com.kuflow.rest.model.Log;
import com.kuflow.rest.model.LogLevel;
import com.kuflow.rest.model.Task;
import com.kuflow.rest.model.TasksDefinitionSummary;
import com.kuflow.temporal.activity.email.EmailActivities;
import com.kuflow.temporal.activity.email.model.Email;
import com.kuflow.temporal.activity.email.model.SendMailRequest;
import com.kuflow.temporal.activity.kuflow.KuFlowActivities;
import com.kuflow.temporal.activity.kuflow.model.AppendTaskLogRequest;
import com.kuflow.temporal.activity.kuflow.model.ClaimTaskRequest;
import com.kuflow.temporal.activity.kuflow.model.CompleteProcessRequest;
import com.kuflow.temporal.activity.kuflow.model.CompleteTaskRequest;
import com.kuflow.temporal.activity.kuflow.model.CompleteTaskResponse;
import com.kuflow.temporal.activity.kuflow.model.CreateTaskRequest;
import com.kuflow.temporal.activity.kuflow.model.RetrieveTaskRequest;
import com.kuflow.temporal.activity.kuflow.model.RetrieveTaskResponse;
import com.kuflow.temporal.common.KuFlowGenerator;
import com.kuflow.temporal.common.model.WorkflowRequest;
import com.kuflow.temporal.common.model.WorkflowResponse;
import com.kuflow.temporal.common.util.TemporalUtils;
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

    private KuFlowGenerator kuflowGenerator;

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
    public WorkflowResponse runWorkflow(WorkflowRequest workflowRequest) {
        this.kuflowGenerator = new KuFlowGenerator(workflowRequest.getProcessId());

        UUID processId = workflowRequest.getProcessId();

        Task taskFillInfo = this.createTaskFillInfo(processId);

        this.createAutomaticTaskSendEmail(processId, taskFillInfo);

        String completeProcessMessage = this.completeProcess(processId);

        return this.completeWorkflow(completeProcessMessage);
    }

    /**
     * Complete the Workflow
     * @param completeProcessMessage message
     * @return the workflow response
     */
    private WorkflowResponse completeWorkflow(String completeProcessMessage) {
        WorkflowResponse workflowResponse = new WorkflowResponse();
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
        CompleteProcessRequest request = new CompleteProcessRequest();
        request.setProcessId(processId);

        this.kuflowActivities.completeProcess(request);

        return "Completed";
    }

    /**
     * Create a task in KuFlow in order to collect the necessary information to send an email.
     *
     * @param processId process identifier
     * @return task created
     */
    private Task createTaskFillInfo(UUID processId) {
        UUID taskId = this.kuflowGenerator.randomUUID();

        TasksDefinitionSummary tasksDefinition = new TasksDefinitionSummary();
        tasksDefinition.setCode(TaskDefinitionCode.FILL_INFO.name());

        Task task = new Task();
        task.setId(taskId);
        task.setProcessId(processId);
        task.setTaskDefinition(tasksDefinition);

        CreateTaskRequest createTaskRequest = new CreateTaskRequest();
        createTaskRequest.setTask(task);

        // Create Task in KuFlow
        this.kuflowActivities.createTaskAndWaitFinished(createTaskRequest);

        RetrieveTaskRequest retrieveTaskRequest = new RetrieveTaskRequest();
        retrieveTaskRequest.setTaskId(taskId);
        RetrieveTaskResponse retrieveTaskResponse = this.kuflowActivities.retrieveTask(retrieveTaskRequest);

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
    private Task createAutomaticTaskSendEmail(UUID processId, Task infoTask) {
        UUID taskId = this.kuflowGenerator.randomUUID();

        TasksDefinitionSummary tasksDefinition = new TasksDefinitionSummary();
        tasksDefinition.setCode(TaskDefinitionCode.SEND_EMAIL.name());

        Task task = new Task();
        task.setId(taskId);
        task.setProcessId(processId);
        task.setTaskDefinition(tasksDefinition);

        CreateTaskRequest createTaskRequest = new CreateTaskRequest();
        createTaskRequest.setTask(task);

        // Create Automatic Task in KuFlow
        this.kuflowActivities.createTask(createTaskRequest);

        // Claim Automatic Task: Our worker will be responsible for its completion.
        ClaimTaskRequest claimTaskRequest = new ClaimTaskRequest();
        claimTaskRequest.setTaskId(taskId);
        this.kuflowActivities.claimTask(claimTaskRequest);

        // Get values from Info Task
        Email email = new Email();
        email.setTemplate("email");
        email.setTo(infoTask.getElementValueAsString(ElementDefinitionCode.EMAIL_RECIPIENT.name()));
        email.addVariables("subject", infoTask.getElementValueAsString(ElementDefinitionCode.EMAIL_SUBJECT.name()));
        email.addVariables("body", infoTask.getElementValueAsString(ElementDefinitionCode.EMAIL_BODY.name()));

        // Send a mail
        SendMailRequest request = new SendMailRequest();
        request.setEmail(email);
        this.emailActivities.sendMail(request);

        // Add some logs to Kuflow task in order to see feedback in Kuflow app
        this.addLogInfoEntryTask(taskId, "Email sent!");

        // Activity Complete
        CompleteTaskRequest completeRequest = new CompleteTaskRequest();
        completeRequest.setTaskId(taskId);
        CompleteTaskResponse completeTaskResponse = this.kuflowActivities.completeTask(completeRequest);

        return completeTaskResponse.getTask();
    }

    /**
     * Add a log message to the referenced KuFlow task
     *
     * @param taskId task identifier
     * @param message a log message
     */
    private void addLogInfoEntryTask(UUID taskId, String message) {
        UUID logId = this.kuflowGenerator.randomUUID();

        Log log = new Log();
        log.setId(logId);
        log.setLevel(LogLevel.INFO);
        log.setMessage(message);

        AppendTaskLogRequest request = new AppendTaskLogRequest();
        request.setTaskId(taskId);
        request.setLog(log);

        this.kuflowActivities.appendTaskLog(request);
    }
}
