/*
 * Copyright (c) 2021-present KuFlow S.L.
 *
 * All rights reserved.
 */

package com.kuflow.engine.samples.worker.loan.workflow;

import com.kuflow.engine.client.activity.kuflow.KuFlowActivities;
import com.kuflow.engine.client.activity.kuflow.resource.CompleteProcessRequestResource;
import com.kuflow.engine.client.activity.kuflow.resource.CompleteProcessResponseResource;
import com.kuflow.engine.client.activity.kuflow.resource.CreateTaskRequestResource;
import com.kuflow.engine.client.activity.kuflow.resource.CreateTaskResponseResource;
import com.kuflow.engine.client.activity.kuflow.resource.RetrieveProcessRequestResource;
import com.kuflow.engine.client.activity.kuflow.resource.RetrieveProcessResponseResource;
import com.kuflow.engine.client.activity.kuflow.resource.RetrieveTaskRequestResource;
import com.kuflow.engine.client.activity.kuflow.resource.RetrieveTaskResponseResource;
import com.kuflow.engine.client.activity.kuflow.resource.TaskAssignRequestResource;
import com.kuflow.engine.client.common.resource.WorkflowRequestResource;
import com.kuflow.engine.client.common.resource.WorkflowResponseResource;
import com.kuflow.engine.client.common.util.TemporalUtils;
import com.kuflow.engine.samples.worker.loan.activity.CurrencyConversionActivities;
import com.kuflow.rest.client.resource.ProcessResource;
import com.kuflow.rest.client.resource.TaskElementValueWrapperResource;
import com.kuflow.rest.client.resource.TaskResource;
import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.workflow.Workflow;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;

public class SampleEngineWorkerLoanWorkflowImpl implements SampleEngineWorkerLoanWorkflow {

    private static final Logger LOGGER = Workflow.getLogger(SampleEngineWorkerLoanWorkflowImpl.class);

    private static final String TASK_LOAN_APPLICATION = "LOAN_APPLICATION";

    private static final String TASK_APPROVE_LOAN = "APPROVE_LOAN";

    private static final String TASK_NOTIFICATION_GRANTED = "NOTIFICATION_GRANTED";

    private static final String TASK_NOTIFICATION_REJECTION = "NOTIFICATION_REJECTION";

    private final KuFlowActivities kuflowActivities;

    private final CurrencyConversionActivities currencyConversionActivities;

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

        this.kuflowActivities =
            Workflow.newActivityStub(
                KuFlowActivities.class,
                defaultActivityOptions,
                Map.of(TemporalUtils.getActivityType(KuFlowActivities.class, "createTaskAndWaitFinished"), asyncActivityOptions)
            );

        this.currencyConversionActivities = Workflow.newActivityStub(CurrencyConversionActivities.class, defaultActivityOptions);
    }

    @Override
    public WorkflowResponseResource runWorkflow(WorkflowRequestResource workflowRequest) {
        LOGGER.info("Started loan process {}", workflowRequest.getProcessId());

        TaskResource taskLoanApplication = this.createTaskLoanApplication(workflowRequest);

        String currency = taskLoanApplication.getElementValues().get("currency").getValueAsString();
        String amount = taskLoanApplication.getElementValues().get("amount").getValueAsString();

        BigDecimal amountEUR = this.convertToEuros(currency, amount);

        TaskResource taskNotification;
        if (amountEUR.compareTo(BigDecimal.valueOf(5_000)) > 0) {
            TaskResource taskApproveLoan = this.createTaskApproveLoan(taskLoanApplication, amountEUR);

            String authorized = taskApproveLoan.getElementValues().get("authorized").getValueAsString();

            if (authorized.equals("OK")) {
                taskNotification = this.createTaskNotificationGranted(workflowRequest);
            } else {
                taskNotification = this.createTaskNotificationRejection(workflowRequest);
            }
        } else {
            taskNotification = this.createTaskNotificationGranted(workflowRequest);
        }

        ProcessResource process = this.retrieveProcess(workflowRequest);

        this.assignTaskToProcessInitiator(taskNotification, process);

        CompleteProcessResponseResource completeProcess = this.completeProcess(workflowRequest);

        LOGGER.info("Finished loan process {}", workflowRequest.getProcessId());

        return this.completeWorkflow(completeProcess);
    }

    private ProcessResource retrieveProcess(WorkflowRequestResource workflowRequest) {
        RetrieveProcessRequestResource request = new RetrieveProcessRequestResource();
        request.setProcessId(workflowRequest.getProcessId());

        RetrieveProcessResponseResource response = this.kuflowActivities.retrieveProcess(request);

        return response.getProcess();
    }

    private WorkflowResponseResource completeWorkflow(CompleteProcessResponseResource completeProcess) {
        WorkflowResponseResource workflowResponse = new WorkflowResponseResource();
        workflowResponse.setMessage(completeProcess.getMessage());

        return workflowResponse;
    }

    private CompleteProcessResponseResource completeProcess(WorkflowRequestResource workflowRequest) {
        CompleteProcessRequestResource request = new CompleteProcessRequestResource();
        request.setProcessId(workflowRequest.getProcessId());

        return this.kuflowActivities.completeProcess(request);
    }

    /**
     * Create a task in KuFlow in order to collect the necessary information to request a loan.
     *
     * @param workflowRequest workflow request
     * @return task created
     */
    private TaskResource createTaskLoanApplication(WorkflowRequestResource workflowRequest) {
        UUID taskId = Workflow.randomUUID();

        CreateTaskRequestResource createTaskRequest = new CreateTaskRequestResource();
        createTaskRequest.setTaskId(taskId); // garantice idempotence
        createTaskRequest.setProcessId(workflowRequest.getProcessId());
        createTaskRequest.setTaskDefinitionCode(TASK_LOAN_APPLICATION);

        this.kuflowActivities.createTaskAndWaitFinished(createTaskRequest);

        RetrieveTaskRequestResource retrieveTaskRequest = new RetrieveTaskRequestResource();
        retrieveTaskRequest.setTaskId(taskId);
        RetrieveTaskResponseResource retrieveTaskResponse = this.kuflowActivities.retrieveTask(retrieveTaskRequest);

        return retrieveTaskResponse.getTask();
    }

    /**
     * Create a task in KuFlow to approve the loan due to doesn't meet the restrictions.
     *
     * @param taskLoanApplication task created to request a loan
     * @param amountEUR amount requested
     * @return task created
     */
    private TaskResource createTaskApproveLoan(TaskResource taskLoanApplication, BigDecimal amountEUR) {
        UUID taskId = Workflow.randomUUID();

        String firstName = taskLoanApplication.getElementValues().get("firstName").getValueAsString();
        String lastName = taskLoanApplication.getElementValues().get("lastName").getValueAsString();

        CreateTaskRequestResource createTaskRequest = new CreateTaskRequestResource();
        createTaskRequest.setTaskId(taskId); // garantice idempotence
        createTaskRequest.setProcessId(taskLoanApplication.getProcessId());
        createTaskRequest.setTaskDefinitionCode(TASK_APPROVE_LOAN);
        createTaskRequest.putElementValuesItem("name", TaskElementValueWrapperResource.of(firstName + " " + lastName));
        createTaskRequest.putElementValuesItem("amountRequested", TaskElementValueWrapperResource.of(amountEUR.toPlainString()));

        this.kuflowActivities.createTaskAndWaitFinished(createTaskRequest);

        RetrieveTaskRequestResource retrieveTaskRequest = new RetrieveTaskRequestResource();
        retrieveTaskRequest.setTaskId(taskId);
        RetrieveTaskResponseResource retrieveTaskResponse = this.kuflowActivities.retrieveTask(retrieveTaskRequest);

        return retrieveTaskResponse.getTask();
    }

    /**
     * Create a notification task showing that the loan was granted.
     *
     * @param workflowRequest workflow request
     * @return task created
     */
    private TaskResource createTaskNotificationGranted(WorkflowRequestResource workflowRequest) {
        CreateTaskRequestResource request = new CreateTaskRequestResource();
        request.setTaskId(Workflow.randomUUID()); // garantice idempotence
        request.setProcessId(workflowRequest.getProcessId());
        request.setTaskDefinitionCode(TASK_NOTIFICATION_GRANTED);

        CreateTaskResponseResource response = this.kuflowActivities.createTask(request);

        return response.getTask();
    }

    /**
     * Create a notification task showing that the loan was rejected.
     *
     * @param workflowRequest workflow request
     * @return task created
     */
    private TaskResource createTaskNotificationRejection(WorkflowRequestResource workflowRequest) {
        CreateTaskRequestResource request = new CreateTaskRequestResource();
        request.setTaskId(Workflow.randomUUID()); // garantice idempotence
        request.setProcessId(workflowRequest.getProcessId());
        request.setTaskDefinitionCode(TASK_NOTIFICATION_REJECTION);

        CreateTaskResponseResource response = this.kuflowActivities.createTask(request);

        return response.getTask();
    }

    private void assignTaskToProcessInitiator(TaskResource taskNotification, ProcessResource process) {
        TaskAssignRequestResource request = new TaskAssignRequestResource();
        request.setTaskId(taskNotification.getId());
        request.setPrincipalId(process.getInitiator().getId());

        this.kuflowActivities.assignTask(request);
    }

    private BigDecimal convertToEuros(String currency, String amount) {
        BigDecimal amountEUR = new BigDecimal(amount != null ? amount : "0");
        if (currency.equals("EUR")) {
            return amountEUR;
        } else {
            String amountText = this.currencyConversionActivities.convert(amountEUR.toPlainString(), currency, "EUR");
            return new BigDecimal(amountText);
        }
    }
}
