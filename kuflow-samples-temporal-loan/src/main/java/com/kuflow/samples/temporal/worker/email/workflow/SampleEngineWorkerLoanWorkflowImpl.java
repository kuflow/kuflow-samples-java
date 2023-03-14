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

import com.kuflow.rest.model.Process;
import com.kuflow.rest.model.Task;
import com.kuflow.rest.model.TaskDefinitionSummary;
import com.kuflow.samples.temporal.worker.email.activity.CurrencyConversionActivities;
import com.kuflow.temporal.activity.kuflow.KuFlowAsyncActivities;
import com.kuflow.temporal.activity.kuflow.KuFlowSyncActivities;
import com.kuflow.temporal.activity.kuflow.model.CompleteProcessRequest;
import com.kuflow.temporal.activity.kuflow.model.CompleteProcessResponse;
import com.kuflow.temporal.activity.kuflow.model.CreateTaskRequest;
import com.kuflow.temporal.activity.kuflow.model.RetrieveProcessRequest;
import com.kuflow.temporal.activity.kuflow.model.RetrieveProcessResponse;
import com.kuflow.temporal.activity.kuflow.model.RetrieveTaskRequest;
import com.kuflow.temporal.activity.kuflow.model.RetrieveTaskResponse;
import com.kuflow.temporal.activity.kuflow.model.SaveProcessElementRequest;
import com.kuflow.temporal.common.KuFlowGenerator;
import com.kuflow.temporal.common.model.WorkflowRequest;
import com.kuflow.temporal.common.model.WorkflowResponse;
import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.workflow.Workflow;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.UUID;
import org.slf4j.Logger;

public class SampleEngineWorkerLoanWorkflowImpl implements SampleEngineWorkerLoanWorkflow {

    private static final Logger LOGGER = Workflow.getLogger(SampleEngineWorkerLoanWorkflowImpl.class);

    private static final String TASK_CODE_APPROVE_LOAN = "TASK_APPROVE_LOAN";

    private static final String TASK_CODE_LOAN_APPLICATION_FORM = "TASK_LOAN_APPLICATION";

    private static final String TASK_CODE_NOTIFICATION_OF_LOAN_GRANTED = "NOTIFICATION_GRANTED";

    private static final String TASK_CODE_NOTIFICATION_OF_LOAN_REJECTION = "NOTIFICATION_REJECTION";

    private final KuFlowSyncActivities kuFlowSyncActivities;

    private final KuFlowAsyncActivities kuFlowAsyncActivities;

    private final CurrencyConversionActivities currencyConversionActivities;

    private KuFlowGenerator kuflowGenerator;

    public SampleEngineWorkerLoanWorkflowImpl() {
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

        this.kuFlowSyncActivities = Workflow.newActivityStub(KuFlowSyncActivities.class, defaultActivityOptions);

        this.kuFlowAsyncActivities = Workflow.newActivityStub(KuFlowAsyncActivities.class, asyncActivityOptions);

        this.currencyConversionActivities = Workflow.newActivityStub(CurrencyConversionActivities.class, defaultActivityOptions);
    }

    @Override
    public WorkflowResponse runWorkflow(WorkflowRequest workflowRequest) {
        LOGGER.info("Started loan process {}", workflowRequest.getProcessId());

        this.kuflowGenerator = new KuFlowGenerator(workflowRequest.getProcessId());

        Task taskLoanApplication = this.createTaskLoanApplicationForm(workflowRequest.getProcessId());

        this.updateProcessMetadata(taskLoanApplication);

        String currency = taskLoanApplication.getElementValueAsString("CURRENCY");
        String amount = taskLoanApplication.getElementValueAsString("AMOUNT");

        // Convert to euros
        BigDecimal amountEUR = this.convertToEuros(currency, amount);

        boolean loanAuthorized = true;
        if (amountEUR.compareTo(BigDecimal.valueOf(5_000)) > 0) {
            Task taskApproveLoan = this.createTaskApproveLoan(taskLoanApplication, amountEUR);

            String approval = taskApproveLoan.getElementValueAsString("APPROVAL");

            loanAuthorized = approval.equals("YES");
        }

        Process process = this.retrieveProcess(workflowRequest);
        if (loanAuthorized) {
            this.createTaskNotificationOfLoanGranted(workflowRequest, process);
        } else {
            this.createTaskNotificationOfLoanGrantedRejection(workflowRequest, process);
        }

        CompleteProcessResponse completeProcess = this.completeProcess(workflowRequest);

        LOGGER.info("Finished loan process {}", workflowRequest.getProcessId());

        return this.completeWorkflow(completeProcess);
    }

    private Process retrieveProcess(WorkflowRequest workflowRequest) {
        RetrieveProcessRequest request = new RetrieveProcessRequest();
        request.setProcessId(workflowRequest.getProcessId());

        RetrieveProcessResponse response = this.kuFlowSyncActivities.retrieveProcess(request);

        return response.getProcess();
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

    private void updateProcessMetadata(Task taskLoanApplication) {
        String firstName = taskLoanApplication.getElementValueAsString("FIRSTNAME");
        String lastName = taskLoanApplication.getElementValueAsString("LASTNAME");

        SaveProcessElementRequest saveFirstNameMetadataRequest = new SaveProcessElementRequest();
        saveFirstNameMetadataRequest.setProcessId(taskLoanApplication.getProcessId());
        saveFirstNameMetadataRequest.setElementDefinitionCode("FIRSTNAME");
        saveFirstNameMetadataRequest.addElementValueAsString(firstName);
        this.kuFlowSyncActivities.saveProcessElement(saveFirstNameMetadataRequest);

        SaveProcessElementRequest saveLastNameMetadataRequest = new SaveProcessElementRequest();
        saveLastNameMetadataRequest.setProcessId(taskLoanApplication.getProcessId());
        saveLastNameMetadataRequest.setElementDefinitionCode("LASTNAME");
        saveLastNameMetadataRequest.addElementValueAsString(lastName);
        this.kuFlowSyncActivities.saveProcessElement(saveLastNameMetadataRequest);
    }


    /**
     * Create a task in KuFlow to approve the loan due to doesn't meet the restrictions.
     *
     * @param taskLoanApplication task created to request a loan
     * @param amountEUR amount requested
     * @return task created
     */
    private Task createTaskApproveLoan(Task taskLoanApplication, BigDecimal amountEUR) {
        String firstName = taskLoanApplication.getElementValueAsString("FIRSTNAME");
        String lastName = taskLoanApplication.getElementValueAsString("LASTNAME");

        UUID taskId = this.kuflowGenerator.randomUUID();

        TaskDefinitionSummary tasksDefinition = new TaskDefinitionSummary();
        tasksDefinition.setCode(TASK_CODE_APPROVE_LOAN);

        Task task = new Task();
        task.setId(taskId);
        task.setProcessId(taskLoanApplication.getProcessId());
        task.setTaskDefinition(tasksDefinition);
        task.setElementValueAsString("FIRSTNAME", firstName);
        task.setElementValueAsString("LASTNAME", lastName);
        task.setElementValueAsString("AMOUNT", amountEUR.toPlainString());

        CreateTaskRequest createTaskRequest = new CreateTaskRequest();
        createTaskRequest.setTask(task);

        this.kuFlowAsyncActivities.createTaskAndWaitFinished(createTaskRequest);

        RetrieveTaskRequest retrieveTaskRequest = new RetrieveTaskRequest();
        retrieveTaskRequest.setTaskId(taskId);
        RetrieveTaskResponse retrieveTaskResponse = this.kuFlowSyncActivities.retrieveTask(retrieveTaskRequest);

        return retrieveTaskResponse.getTask();
    }

    /**
     * Create a task in KuFlow in order to collect the necessary information to request a loan.
     *
     * @param workflowRequest workflow request
     * @return task created
     */
    private Task createTaskLoanApplicationForm(UUID processId) {
        UUID taskId = this.kuflowGenerator.randomUUID();

        TaskDefinitionSummary tasksDefinition = new TaskDefinitionSummary();
        tasksDefinition.setCode(TASK_CODE_LOAN_APPLICATION_FORM);

        Task task = new Task();
        task.setId(taskId);
        task.setProcessId(processId);
        task.setTaskDefinition(tasksDefinition);

        CreateTaskRequest createTaskRequest = new CreateTaskRequest();
        createTaskRequest.setTask(task);

        this.kuFlowAsyncActivities.createTaskAndWaitFinished(createTaskRequest);

        RetrieveTaskRequest retrieveTaskRequest = new RetrieveTaskRequest();
        retrieveTaskRequest.setTaskId(taskId);
        RetrieveTaskResponse retrieveTaskResponse = this.kuFlowSyncActivities.retrieveTask(retrieveTaskRequest);

        return retrieveTaskResponse.getTask();
    }

    /**
     * Create a notification task showing that the loan was granted.
     *
     * @param workflowRequest workflow request
     * @param process Related process
     */
    private void createTaskNotificationOfLoanGranted(WorkflowRequest workflowRequest, Process process) {
        UUID taskId = this.kuflowGenerator.randomUUID();

        TaskDefinitionSummary tasksDefinition = new TaskDefinitionSummary();
        tasksDefinition.setCode(TASK_CODE_NOTIFICATION_OF_LOAN_GRANTED);

        Task task = new Task();
        task.setId(taskId);
        task.setProcessId(workflowRequest.getProcessId());
        task.setTaskDefinition(tasksDefinition);
        task.setOwner(process.getInitiator());

        CreateTaskRequest request = new CreateTaskRequest();
        request.setTask(task);

        this.kuFlowSyncActivities.createTask(request);
    }

    /**
     * Create a notification task showing that the loan was rejected.
     *
     * @param workflowRequest workflow request
     * @param process Related process
     */
    private void createTaskNotificationOfLoanGrantedRejection(WorkflowRequest workflowRequest, Process process) {
        UUID taskId = this.kuflowGenerator.randomUUID();

        TaskDefinitionSummary tasksDefinition = new TaskDefinitionSummary();
        tasksDefinition.setCode(TASK_CODE_NOTIFICATION_OF_LOAN_REJECTION);

        Task task = new Task();
        task.setId(taskId);
        task.setProcessId(workflowRequest.getProcessId());
        task.setTaskDefinition(tasksDefinition);
        task.setOwner(process.getInitiator());

        CreateTaskRequest request = new CreateTaskRequest();
        request.setTask(task);

        this.kuFlowSyncActivities.createTask(request);
    }

    private BigDecimal convertToEuros(String currency, String amount) {
        BigDecimal amountNumber = new BigDecimal(amount != null ? amount : "0");
        if (currency.equals("EUR")) {
            return amountNumber;
        }

        String amountText = this.currencyConversionActivities.convert(amountNumber.toPlainString(), currency, "EUR");
        return new BigDecimal(amountText);
    }
}
