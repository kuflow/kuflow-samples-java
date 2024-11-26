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
package com.kuflow.samples.temporal.worker.loan.workflow;

import com.kuflow.rest.model.JsonPatchOperation;
import com.kuflow.rest.model.JsonPatchOperationType;
import com.kuflow.rest.model.JsonValue;
import com.kuflow.rest.model.Process;
import com.kuflow.rest.model.ProcessItem;
import com.kuflow.rest.model.ProcessItemTaskCreateParams;
import com.kuflow.rest.model.ProcessItemType;
import com.kuflow.samples.temporal.worker.loan.activity.CurrencyConversionActivities;
import com.kuflow.temporal.activity.kuflow.KuFlowActivities;
import com.kuflow.temporal.activity.kuflow.model.ProcessItemCreateRequest;
import com.kuflow.temporal.activity.kuflow.model.ProcessItemRetrieveRequest;
import com.kuflow.temporal.activity.kuflow.model.ProcessItemRetrieveResponse;
import com.kuflow.temporal.activity.kuflow.model.ProcessMetadataPatchRequest;
import com.kuflow.temporal.activity.kuflow.model.ProcessRetrieveRequest;
import com.kuflow.temporal.activity.kuflow.model.ProcessRetrieveResponse;
import com.kuflow.temporal.workflow.kuflow.KuFlowWorkflow;
import com.kuflow.temporal.workflow.kuflow.model.SignalProcessItem;
import com.kuflow.temporal.workflow.kuflow.model.SignalProcessItemType;
import com.kuflow.temporal.workflow.kuflow.model.WorkflowRequest;
import com.kuflow.temporal.workflow.kuflow.model.WorkflowResponse;
import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.workflow.Workflow;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;

public class SampleEngineWorkerLoanWorkflowImpl implements SampleEngineWorkerLoanWorkflow {

    private static final Logger LOGGER = Workflow.getLogger(SampleEngineWorkerLoanWorkflowImpl.class);

    private static final String TASK_CODE_APPROVE_LOAN = "APPROVE_LOAN";

    private static final String TASK_CODE_LOAN_APPLICATION_FORM = "LOAN_APPLICATION";

    private static final String TASK_CODE_NOTIFICATION_OF_LOAN_GRANTED = "NOTIFICATION_GRANTED";

    private static final String TASK_CODE_NOTIFICATION_OF_LOAN_REJECTION = "NOTIFICATION_REJECTION";

    private final KuFlowActivities kuFlowActivities;

    private final CurrencyConversionActivities currencyConversionActivities;

    private final Set<UUID> kuFlowCompletedTaskIds = new HashSet<>();

    public SampleEngineWorkerLoanWorkflowImpl() {
        RetryOptions defaultRetryOptions = RetryOptions.newBuilder().validateBuildWithDefaults();

        ActivityOptions defaultActivityOptions = ActivityOptions.newBuilder()
            .setRetryOptions(defaultRetryOptions)
            .setStartToCloseTimeout(Duration.ofMinutes(10))
            .setScheduleToCloseTimeout(Duration.ofDays(365))
            .validateAndBuildWithDefaults();

        this.kuFlowActivities = Workflow.newActivityStub(KuFlowActivities.class, defaultActivityOptions);

        this.currencyConversionActivities = Workflow.newActivityStub(CurrencyConversionActivities.class, defaultActivityOptions);
    }

    @Override
    public WorkflowResponse runWorkflow(WorkflowRequest workflowRequest) {
        LOGGER.info("Started loan process {}", workflowRequest.getProcessId());

        ProcessItem processItemLoanApplication = this.createProcessItemLoanApplication(workflowRequest.getProcessId());

        this.updateProcessMetadata(processItemLoanApplication);

        String currency = processItemLoanApplication.getTask().getData().getValue().get("CURRENCY").toString();
        String amount = processItemLoanApplication.getTask().getData().getValue().get("AMOUNT").toString();

        // Convert to euros
        BigDecimal amountEUR = this.convertToEuros(currency, amount);

        boolean loanAuthorized = true;
        if (amountEUR.compareTo(BigDecimal.valueOf(5_000)) > 0) {
            ProcessItem processItemApproveLoan = this.createProcessItemApproveLoan(processItemLoanApplication, amountEUR);

            String approval = processItemApproveLoan.getTask().getData().getValue().get("APPROVAL").toString();

            loanAuthorized = "YES".equals(approval);
        }

        Process process = this.retrieveProcess(workflowRequest);
        if (loanAuthorized) {
            this.createProcessItemTaskNotificationOfLoanGranted(workflowRequest, process);
        } else {
            this.createProcessItemNotificationOfLoanGrantedRejection(workflowRequest, process);
        }

        LOGGER.info("Finished loan process {}", workflowRequest.getProcessId());

        return this.completeWorkflow(workflowRequest);
    }

    @Override
    public void handleKuFlowEngineSignalProcessItem(SignalProcessItem signal) {
        if (SignalProcessItemType.TASK.equals(signal.getType())) {
            this.kuFlowCompletedTaskIds.add(signal.getId());
        }
    }

    private Process retrieveProcess(WorkflowRequest workflowRequest) {
        ProcessRetrieveRequest request = new ProcessRetrieveRequest();
        request.setProcessId(workflowRequest.getProcessId());

        ProcessRetrieveResponse response = this.kuFlowActivities.retrieveProcess(request);

        return response.getProcess();
    }

    private WorkflowResponse completeWorkflow(WorkflowRequest workflowRequest) {
        WorkflowResponse workflowResponse = new WorkflowResponse();
        workflowResponse.setMessage("Complete process " + workflowRequest.getProcessId());

        return workflowResponse;
    }

    private void updateProcessMetadata(ProcessItem processItemLoanApplication) {
        String firstName = processItemLoanApplication.getTask().getData().getValue().get("FIRST_NAME").toString();
        String lastName = processItemLoanApplication.getTask().getData().getValue().get("LAST_NAME").toString();

        JsonPatchOperation firstNameJsonPatchOperation = new JsonPatchOperation();
        firstNameJsonPatchOperation.setOp(JsonPatchOperationType.ADD);
        firstNameJsonPatchOperation.setPath("/FIRST_NAME");
        firstNameJsonPatchOperation.setValue(firstName);

        JsonPatchOperation lastNameJsonPatchOperation = new JsonPatchOperation();
        lastNameJsonPatchOperation.setOp(JsonPatchOperationType.ADD);
        lastNameJsonPatchOperation.setPath("/LAST_NAME");
        lastNameJsonPatchOperation.setValue(lastName);

        ProcessMetadataPatchRequest patchRequest = new ProcessMetadataPatchRequest();
        patchRequest.setProcessId(processItemLoanApplication.getProcessId());
        patchRequest.setJsonPatch(List.of(firstNameJsonPatchOperation, lastNameJsonPatchOperation));

        this.kuFlowActivities.patchProcessMetadata(patchRequest);
    }

    /**
     * Create a task in KuFlow to approve the loan due to doesn't meet the restrictions.
     *
     * @param processItemLoanApplication task created to request a loan
     * @param amountEUR amount requested
     * @return process item created
     */
    private ProcessItem createProcessItemApproveLoan(ProcessItem processItemLoanApplication, BigDecimal amountEUR) {
        String firstName = processItemLoanApplication.getTask().getData().getValue().get("FIRST_NAME").toString();
        String lastName = processItemLoanApplication.getTask().getData().getValue().get("LAST_NAME").toString();

        UUID processItemId = KuFlowWorkflow.generateUUIDv7();

        JsonValue createTaskData = new JsonValue();
        createTaskData.setValue(Map.of("FIRST_NAME", firstName, "LAST_NAME", lastName, "AMOUNT", amountEUR.toPlainString()));

        ProcessItemTaskCreateParams createTaskRequest = new ProcessItemTaskCreateParams();
        createTaskRequest.setData(createTaskData);

        ProcessItemCreateRequest createRequest = new ProcessItemCreateRequest();
        createRequest.setId(processItemId);
        createRequest.setType(ProcessItemType.TASK);
        createRequest.setProcessId(processItemLoanApplication.getProcessId());
        createRequest.setProcessItemDefinitionCode(TASK_CODE_APPROVE_LOAN);
        createRequest.setTask(createTaskRequest);

        this.createProcessItemAndWaitCompleted(createRequest);

        ProcessItemRetrieveRequest retrieveRequest = new ProcessItemRetrieveRequest();
        retrieveRequest.setProcessItemId(processItemId);
        ProcessItemRetrieveResponse retrieveTaskResponse = this.kuFlowActivities.retrieveProcessItem(retrieveRequest);

        return retrieveTaskResponse.getProcessItem();
    }

    /**
     * Create a task in KuFlow in order to collect the necessary information to request a loan.
     *
     * @param processId Process ID
     * @return task created
     */
    private ProcessItem createProcessItemLoanApplication(UUID processId) {
        UUID processItemId = KuFlowWorkflow.generateUUIDv7();

        ProcessItemCreateRequest createRequest = new ProcessItemCreateRequest();
        createRequest.setId(processItemId);
        createRequest.setType(ProcessItemType.TASK);
        createRequest.setProcessId(processId);
        createRequest.setProcessItemDefinitionCode(TASK_CODE_LOAN_APPLICATION_FORM);

        this.createProcessItemAndWaitCompleted(createRequest);

        ProcessItemRetrieveRequest retrieveRequest = new ProcessItemRetrieveRequest();
        retrieveRequest.setProcessItemId(processItemId);
        ProcessItemRetrieveResponse retrieveResponse = this.kuFlowActivities.retrieveProcessItem(retrieveRequest);

        return retrieveResponse.getProcessItem();
    }

    /**
     * Create a notification process item showing that the loan was granted.
     *
     * @param workflowRequest workflow request
     * @param process Related process
     */
    private void createProcessItemTaskNotificationOfLoanGranted(WorkflowRequest workflowRequest, Process process) {
        UUID processItemId = KuFlowWorkflow.generateUUIDv7();

        ProcessItemCreateRequest request = new ProcessItemCreateRequest();
        request.setId(processItemId);
        request.setProcessId(workflowRequest.getProcessId());
        request.setType(ProcessItemType.TASK);
        request.setProcessItemDefinitionCode(TASK_CODE_NOTIFICATION_OF_LOAN_GRANTED);
        request.setOwnerId(process.getInitiatorId());

        this.kuFlowActivities.createProcessItem(request);
    }

    /**
     * Create a notification process item showing that the loan was rejected.
     *
     * @param workflowRequest workflow request
     * @param process Related process
     */
    private void createProcessItemNotificationOfLoanGrantedRejection(WorkflowRequest workflowRequest, Process process) {
        UUID processItemId = KuFlowWorkflow.generateUUIDv7();

        ProcessItemCreateRequest request = new ProcessItemCreateRequest();
        request.setId(processItemId);
        request.setProcessId(workflowRequest.getProcessId());
        request.setType(ProcessItemType.TASK);
        request.setProcessItemDefinitionCode(TASK_CODE_NOTIFICATION_OF_LOAN_REJECTION);
        request.setOwnerId(process.getInitiatorId());

        this.kuFlowActivities.createProcessItem(request);
    }

    private BigDecimal convertToEuros(String currency, String amount) {
        BigDecimal amountNumber = new BigDecimal(amount != null ? amount : "0");
        if ("EUR".equals(currency)) {
            return amountNumber;
        }

        String amountText = this.currencyConversionActivities.convert(amountNumber.toPlainString(), currency, "EUR");
        return new BigDecimal(amountText);
    }

    /**
     * Create a process item and wait for the task will be completed
     * @param request process item task to create
     */
    private void createProcessItemAndWaitCompleted(ProcessItemCreateRequest request) {
        this.kuFlowActivities.createProcessItem(request);

        // Wait for completion
        Workflow.await(() -> this.kuFlowCompletedTaskIds.contains(request.getId()));
    }
}
