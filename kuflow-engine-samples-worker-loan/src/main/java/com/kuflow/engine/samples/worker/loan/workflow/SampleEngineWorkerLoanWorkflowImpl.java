/*
 * Copyright (c) 2021-present KuFlow S.L.
 *
 * All rights reserved.
 */

package com.kuflow.engine.samples.worker.loan.workflow;

import com.kuflow.engine.client.activity.kuflow.KuFlowActivities;
import com.kuflow.engine.client.activity.kuflow.resource.CompleteProcessRequestResource;
import com.kuflow.engine.client.activity.kuflow.resource.CompleteProcessResponseResource;
import com.kuflow.engine.client.activity.kuflow.resource.StartProcessRequestResource;
import com.kuflow.engine.client.activity.kuflow.resource.StartProcessResponseResource;
import com.kuflow.engine.client.activity.kuflow.resource.TaskAssignRequestResource;
import com.kuflow.engine.client.activity.kuflow.resource.TaskRequestResource;
import com.kuflow.engine.client.activity.kuflow.resource.TaskResponseResource;
import com.kuflow.engine.client.common.resource.WorkflowRequestResource;
import com.kuflow.engine.client.common.resource.WorkflowResponseResource;
import com.kuflow.engine.client.common.util.TemporalUtils;
import com.kuflow.engine.samples.worker.loan.activity.CurrencyConversionActivities;
import com.kuflow.rest.client.resource.ElementValueWrapperResource;
import com.kuflow.rest.client.resource.ProcessResource;
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
                Map.of(TemporalUtils.getActivityType(KuFlowActivities.class, "createTaskAndWaitTermination"), asyncActivityOptions)
            );

        this.currencyConversionActivities = Workflow.newActivityStub(CurrencyConversionActivities.class, defaultActivityOptions);
    }

    @Override
    public WorkflowResponseResource runWorkflow(WorkflowRequestResource workflowRequest) {
        LOGGER.info("Started loan process {}", workflowRequest.getProcessId());

        ProcessResource process = this.startProcess(workflowRequest.getProcessId());

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

        this.assignTaskToProcessInitiator(taskNotification, process);

        CompleteProcessResponseResource completeProcess = this.completeProcess(workflowRequest.getProcessId());

        LOGGER.info("Finished loan process {}", workflowRequest.getProcessId());

        return this.completeWorkflow(completeProcess);
    }

    private WorkflowResponseResource completeWorkflow(CompleteProcessResponseResource completeProcess) {
        WorkflowResponseResource workflowResponse = new WorkflowResponseResource();
        workflowResponse.setMessage(completeProcess.getMessage());

        return workflowResponse;
    }

    private ProcessResource startProcess(UUID processId) {
        StartProcessRequestResource request = new StartProcessRequestResource();
        request.setProcessId(processId);

        StartProcessResponseResource response = this.kuflowActivities.startProcess(request);

        return response.getProcess();
    }

    private CompleteProcessResponseResource completeProcess(UUID processId) {
        CompleteProcessRequestResource request = new CompleteProcessRequestResource();
        request.setProcessId(processId);

        return this.kuflowActivities.completeProcess(request);
    }

    /**
     * Create a task in KuFlow in order to collect the necessary information to request a loan.
     *
     * @param workflowRequest workflow request
     * @return task created
     */
    private TaskResource createTaskLoanApplication(WorkflowRequestResource workflowRequest) {
        TaskRequestResource request = new TaskRequestResource();
        request.setTaskId(Workflow.randomUUID()); // garantice idempotence
        request.setProcessId(workflowRequest.getProcessId());
        request.setTaskDefinitionCode(TASK_LOAN_APPLICATION);

        TaskResponseResource response = this.kuflowActivities.createTaskAndWaitTermination(request);

        return response.getTask();
    }

    /**
     * Create a task in KuFlow to approve the loan due to doesn't meet the restrictions.
     *
     * @param taskLoanApplication task created to request a loan
     * @param amountEUR amount requested
     * @return task created
     */
    private TaskResource createTaskApproveLoan(TaskResource taskLoanApplication, BigDecimal amountEUR) {
        String firstName = taskLoanApplication.getElementValues().get("firstName").getValueAsString();
        String lastName = taskLoanApplication.getElementValues().get("lastName").getValueAsString();

        TaskRequestResource request = new TaskRequestResource();
        request.setTaskId(Workflow.randomUUID()); // garantice idempotence
        request.setProcessId(taskLoanApplication.getProcessId());
        request.setTaskDefinitionCode(TASK_APPROVE_LOAN);
        request.putElementValue("name", ElementValueWrapperResource.of(firstName + " " + lastName));
        request.putElementValue("amountRequested", ElementValueWrapperResource.of(amountEUR.toPlainString()));

        TaskResponseResource response = this.kuflowActivities.createTaskAndWaitTermination(request);

        return response.getTask();
    }

    /**
     * Create a notification task showing that the loan was granted.
     *
     * @param workflowRequest workflow request
     * @return task created
     */
    private TaskResource createTaskNotificationGranted(WorkflowRequestResource workflowRequest) {
        TaskRequestResource request = new TaskRequestResource();
        request.setTaskId(Workflow.randomUUID()); // garantice idempotence
        request.setProcessId(workflowRequest.getProcessId());
        request.setTaskDefinitionCode(TASK_NOTIFICATION_GRANTED);

        TaskResponseResource response = this.kuflowActivities.createTask(request);

        return response.getTask();
    }

    /**
     * Create a notification task showing that the loan was rejected.
     *
     * @param workflowRequest workflow request
     * @return task created
     */
    private TaskResource createTaskNotificationRejection(WorkflowRequestResource workflowRequest) {
        TaskRequestResource request = new TaskRequestResource();
        request.setTaskId(Workflow.randomUUID()); // garantice idempotence
        request.setProcessId(workflowRequest.getProcessId());
        request.setTaskDefinitionCode(TASK_NOTIFICATION_REJECTION);

        TaskResponseResource response = this.kuflowActivities.createTask(request);

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
