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
package com.kuflow.samples.rest.worker.loan;

import com.kuflow.rest.KuFlowRestClient;
import com.kuflow.rest.model.DefaultErrorException;
import com.kuflow.rest.model.JsonValue;
import com.kuflow.rest.model.Process;
import com.kuflow.rest.model.ProcessItem;
import com.kuflow.rest.model.ProcessItemCreateParams;
import com.kuflow.rest.model.ProcessItemTaskAssignParams;
import com.kuflow.rest.model.ProcessItemTaskCreateParams;
import com.kuflow.rest.model.ProcessItemTaskState;
import com.kuflow.rest.model.ProcessItemType;
import com.kuflow.rest.model.ProcessState;
import com.kuflow.rest.model.WebhookEvent;
import com.kuflow.rest.model.WebhookEventProcessItemTaskStateChanged;
import com.kuflow.rest.model.WebhookEventProcessItemTaskStateChangedData;
import com.kuflow.rest.model.WebhookEventProcessStateChanged;
import com.kuflow.rest.model.WebhookEventProcessStateChangedData;
import com.kuflow.rest.operation.ProcessItemOperations;
import com.kuflow.rest.operation.ProcessOperations;
import com.kuflow.samples.rest.worker.loan.util.CastUtils;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/webhooks")
public class SampleRestWorkerLoanController {

    private static final Logger LOGGER = LoggerFactory.getLogger(SampleRestWorkerLoanController.class);

    private static final String TASK_CODE_APPROVE_LOAN = "APPROVE_LOAN";

    private static final String TASK_CODE_LOAN_APPLICATION_FORM = "LOAN_APPLICATION";

    private static final String TASK_CODE_NOTIFICATION_OF_LOAN_GRANTED = "NOTIFICATION_GRANTED";

    private static final String TASK_CODE_NOTIFICATION_OF_LOAN_REJECTION = "NOTIFICATION_REJECTION";

    private final RestTemplate restTemplate;

    private final KuFlowRestClient kuFlowRestClient;

    private final ProcessOperations processOperations;

    private final ProcessItemOperations processItemOperations;

    public SampleRestWorkerLoanController(RestTemplateBuilder restTemplateBuilder, KuFlowRestClient kuFlowRestClient) {
        this.restTemplate = restTemplateBuilder.setConnectTimeout(Duration.ofSeconds(500)).setReadTimeout(Duration.ofSeconds(500)).build();
        this.kuFlowRestClient = kuFlowRestClient;
        this.processOperations = kuFlowRestClient.getProcessOperations();
        this.processItemOperations = kuFlowRestClient.getProcessItemOperations();
    }

    @PostMapping
    public void handleEvent(@RequestBody String payload) {
        LOGGER.info("Event {}", payload);

        WebhookEvent event = this.kuFlowRestClient.parseWebhookEvent(payload);

        try {
            if (event instanceof WebhookEventProcessStateChanged) {
                this.handleEventProcessStateChanged(CastUtils.cast(event));
            } else if (event instanceof WebhookEventProcessItemTaskStateChanged) {
                this.handleEventProcessItemTaskStateChanged(CastUtils.cast(event));
            }
        } catch (DefaultErrorException ex) {
            if (HttpStatus.FORBIDDEN.equals(HttpStatus.valueOf(ex.getValue().getStatus()))) {
                LOGGER.error(
                    String.format(
                        "The resource cannot be accessed, the process may be completed or cancelled. We ignore this event. Id: %s",
                        event.getId()
                    ),
                    ex
                );
            } else if (HttpStatus.CONFLICT.equals(HttpStatus.valueOf(ex.getValue().getStatus()))) {
                LOGGER.error(String.format("Invalid state of resource. We ignore this event. Id: %s", event.getId()), ex);
            } else {
                throw ex;
            }
        }
    }

    private void handleEventProcessStateChanged(WebhookEventProcessStateChanged event) {
        WebhookEventProcessStateChangedData data = event.getData();
        if (ProcessState.RUNNING.equals(data.getProcessState())) {
            this.createProcessItemTaskLoanApplication(data);
        }
    }

    private void handleEventProcessItemTaskStateChanged(WebhookEventProcessItemTaskStateChanged event) {
        WebhookEventProcessItemTaskStateChangedData data = event.getData();
        if (
            TASK_CODE_LOAN_APPLICATION_FORM.equals(data.getProcessItemTaskCode()) &&
            ProcessItemTaskState.COMPLETED.equals(data.getProcessItemState())
        ) {
            this.handleProcessItemLoanApplication(data);
        }
        if (
            TASK_CODE_APPROVE_LOAN.equals(data.getProcessItemTaskCode()) &&
            ProcessItemTaskState.COMPLETED.equals(data.getProcessItemState())
        ) {
            this.handleProcessItemApproveLoan(data);
        }
    }

    private void handleProcessItemApproveLoan(WebhookEventProcessItemTaskStateChangedData data) {
        ProcessItem processItemApproveLoan = this.processItemOperations.retrieveProcessItem(data.getProcessItemId());

        String authorizedField = processItemApproveLoan.getTask().getData().getValue().get("APPROVAL").toString();

        ProcessItem processItemNotification;
        if ("YES".equals(authorizedField)) {
            processItemNotification = this.createProcessItemTaskNotificationOfLoanGranted(data);
        } else {
            processItemNotification = this.createProcessItemTaskNotificationOfLoanGrantedRejection(data);
        }

        Process process = this.processOperations.retrieveProcess(data.getProcessId());

        this.assignProcessItemTaskToProcessInitiator(processItemNotification, process);

        this.processOperations.completeProcess(data.getProcessId());
    }

    private void handleProcessItemLoanApplication(WebhookEventProcessItemTaskStateChangedData data) {
        ProcessItem processItemLoanApplication = this.processItemOperations.retrieveProcessItem(data.getProcessItemId());

        String currencyField = processItemLoanApplication.getTask().getData().getValue().get("CURRENCY").toString();
        String amountField = processItemLoanApplication.getTask().getData().getValue().get("AMOUNT").toString();

        BigDecimal amountEUR = this.convertToEuros(currencyField, amountField);

        if (amountEUR.compareTo(BigDecimal.valueOf(5000)) > 0) {
            this.createProcessItemTaskApproveLoan(processItemLoanApplication, amountEUR);
        } else {
            ProcessItem processItemNotification = this.createProcessItemTaskNotificationOfLoanGranted(data);

            Process process = this.processOperations.retrieveProcess(data.getProcessId());

            this.assignProcessItemTaskToProcessInitiator(processItemNotification, process);

            this.processOperations.completeProcess(data.getProcessId());
        }
    }

    private void createProcessItemTaskLoanApplication(WebhookEventProcessStateChangedData data) {
        ProcessItemTaskCreateParams paramsTask = new ProcessItemTaskCreateParams();
        paramsTask.setTaskDefinitionCode(TASK_CODE_LOAN_APPLICATION_FORM);

        ProcessItemCreateParams params = new ProcessItemCreateParams();
        params.setProcessId(data.getProcessId());
        params.setType(ProcessItemType.TASK);
        params.setTask(paramsTask);

        this.processItemOperations.createProcessItem(params);
    }

    private void createProcessItemTaskApproveLoan(ProcessItem processItemLoanApplication, BigDecimal amountEUR) {
        String firstName = processItemLoanApplication.getTask().getData().getValue().get("FIRST_NAME").toString();
        String lastName = processItemLoanApplication.getTask().getData().getValue().get("LAST_NAME").toString();

        JsonValue paramsTaskData = new JsonValue();
        paramsTaskData.setValue(Map.of("FIRST_NAME", firstName, "LAST_NAME", lastName, "AMOUNT", amountEUR.toPlainString()));

        ProcessItemTaskCreateParams paramsTask = new ProcessItemTaskCreateParams();
        paramsTask.setTaskDefinitionCode(TASK_CODE_APPROVE_LOAN);
        paramsTask.setData(paramsTaskData);

        ProcessItemCreateParams params = new ProcessItemCreateParams();
        params.setProcessId(processItemLoanApplication.getProcessId());
        params.setType(ProcessItemType.TASK);
        params.setTask(paramsTask);

        this.processItemOperations.createProcessItem(params);
    }

    private ProcessItem createProcessItemTaskNotificationOfLoanGrantedRejection(WebhookEventProcessItemTaskStateChangedData data) {
        ProcessItemTaskCreateParams processItemTask = new ProcessItemTaskCreateParams();
        processItemTask.setTaskDefinitionCode(TASK_CODE_NOTIFICATION_OF_LOAN_REJECTION);

        ProcessItemCreateParams processItemNotificationRejection = new ProcessItemCreateParams();
        processItemNotificationRejection.setProcessId(data.getProcessId());
        processItemNotificationRejection.setType(ProcessItemType.TASK);
        processItemNotificationRejection.setTask(processItemTask);

        return this.processItemOperations.createProcessItem(processItemNotificationRejection);
    }

    private ProcessItem createProcessItemTaskNotificationOfLoanGranted(WebhookEventProcessItemTaskStateChangedData data) {
        ProcessItemTaskCreateParams paramsTask = new ProcessItemTaskCreateParams();
        paramsTask.setTaskDefinitionCode(TASK_CODE_NOTIFICATION_OF_LOAN_GRANTED);

        ProcessItemCreateParams params = new ProcessItemCreateParams();
        params.setType(ProcessItemType.TASK);
        params.setProcessId(data.getProcessId());
        params.setTask(paramsTask);

        return this.processItemOperations.createProcessItem(params);
    }

    private void assignProcessItemTaskToProcessInitiator(ProcessItem processItemNotification, Process process) {
        ProcessItemTaskAssignParams params = new ProcessItemTaskAssignParams();
        params.setOwnerId(process.getInitiatorId());

        this.processItemOperations.assignProcessItemTask(processItemNotification.getId(), params);
    }

    private BigDecimal convertToEuros(String currencyField, String amountField) {
        BigDecimal amountEUR = new BigDecimal(amountField != null ? amountField : "0");
        if ("EUR".equals(currencyField)) {
            return amountEUR;
        } else {
            return this.convert(amountEUR, currencyField, "EUR");
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private BigDecimal convert(BigDecimal amount, String from, String to) {
        String fromTransformed = this.transformCurrencyCode(from);
        String toTransformed = this.transformCurrencyCode(to);
        String endpoint = String.format(
            "https://cdn.jsdelivr.net/npm/@fawazahmed0/currency-api@latest/v1/currencies/%s.json",
            fromTransformed
        );

        ParameterizedTypeReference<HashMap<String, Object>> responseType = new ParameterizedTypeReference<>() {};
        RequestEntity<Void> request = RequestEntity.get(endpoint).build();
        HashMap<String, Object> response = this.restTemplate.exchange(request, responseType).getBody();

        Map<String, Double> conversionTable = (Map) response.get(fromTransformed);
        Double conversion = conversionTable.get(toTransformed);

        return amount.multiply(BigDecimal.valueOf(conversion));
    }

    private String transformCurrencyCode(String currency) {
        return switch (currency) {
            case "EUR" -> "eur";
            case "USD" -> "usd";
            case "GBP" -> "gbp";
            default -> throw new RuntimeException("Unsupported currency " + currency);
        };
    }
}
