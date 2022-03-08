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
import com.kuflow.rest.client.resource.ElementDefinitionTypeResource;
import com.kuflow.rest.client.resource.ElementValueDecisionResource;
import com.kuflow.rest.client.resource.ElementValueFieldResource;
import com.kuflow.rest.client.resource.ProcessResource;
import com.kuflow.rest.client.resource.TaskResource;
import com.kuflow.rest.client.util.ElementUtils;
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
        ElementValueDecisionResource currencyField = this.retrieveElementDecision(taskLoanApplication, "currency");
        ElementValueFieldResource amountField = this.retrieveElementValue(taskLoanApplication, "amount");

        BigDecimal amountEUR = this.convertToEuros(currencyField, amountField);

        TaskResource taskNotification;
        if (amountEUR.compareTo(BigDecimal.valueOf(5_000)) > 0) {
            TaskResource taskApproveLoan = this.createTaskApproveLoan(taskLoanApplication, amountEUR);
            ElementValueDecisionResource authorizedField = this.retrieveElementDecision(taskApproveLoan, "authorized");

            if (authorizedField.getCode().equals("OK")) {
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
        ElementValueFieldResource firstNameField = this.retrieveElementValue(taskLoanApplication, "firstName");
        ElementValueFieldResource lastNameField = this.retrieveElementValue(taskLoanApplication, "lastName");

        ElementValueFieldResource nameField = new ElementValueFieldResource();
        nameField.setElementDefinitionType(ElementDefinitionTypeResource.FIELD);
        nameField.setElementDefinitionCode("name");
        nameField.setValue(firstNameField.getValue() + " " + lastNameField.getValue());

        ElementValueFieldResource amountRequestedField = new ElementValueFieldResource();
        amountRequestedField.setElementDefinitionType(ElementDefinitionTypeResource.FIELD);
        amountRequestedField.setElementDefinitionCode("amountRequested");
        amountRequestedField.setValue(amountEUR.toPlainString());

        TaskRequestResource request = new TaskRequestResource();
        request.setTaskId(Workflow.randomUUID()); // garantice idempotence
        request.setProcessId(taskLoanApplication.getProcessId());
        request.setTaskDefinitionCode(TASK_APPROVE_LOAN);
        request.addElementValue(nameField);
        request.addElementValue(amountRequestedField);

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

    private BigDecimal convertToEuros(ElementValueDecisionResource currencyField, ElementValueFieldResource amountField) {
        BigDecimal amount = new BigDecimal(amountField.getValue() != null ? amountField.getValue() : "0");
        if (currencyField.getCode().equals("EUR")) {
            return amount;
        } else {
            String amountText = this.currencyConversionActivities.convert(amount.toPlainString(), currencyField.getCode(), "EUR");
            return new BigDecimal(amountText);
        }
    }

    private ElementValueDecisionResource retrieveElementDecision(TaskResource taskLoanApplication, String code) {
        return ElementUtils.getSingleValueByCode(taskLoanApplication, code, ElementValueDecisionResource.class);
    }

    private ElementValueFieldResource retrieveElementValue(TaskResource taskLoanApplication, String code) {
        return ElementUtils.getSingleValueByCode(taskLoanApplication, code, ElementValueFieldResource.class);
    }
}
