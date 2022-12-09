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
import com.kuflow.rest.model.Process;
import com.kuflow.rest.model.ProcessState;
import com.kuflow.rest.model.Task;
import com.kuflow.rest.model.TaskAssignCommand;
import com.kuflow.rest.model.TaskState;
import com.kuflow.rest.model.TasksDefinitionSummary;
import com.kuflow.rest.model.WebhookEvent;
import com.kuflow.rest.model.WebhookEventProcessStateChanged;
import com.kuflow.rest.model.WebhookEventProcessStateChangedData;
import com.kuflow.rest.model.WebhookEventTaskStateChanged;
import com.kuflow.rest.model.WebhookEventTaskStateChangedData;
import com.kuflow.samples.rest.worker.loan.util.CastUtils;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
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

    private static final String TASK_LOAN_APPLICATION = "LOAN_APPLICATION";

    private static final String TASK_APPROVE_LOAN = "APPROVE_LOAN";

    private static final String TASK_NOTIFICATION_REJECTION = "NOTIFICATION_REJECTION";

    private static final String TASK_NOTIFICATION_GRANTED = "NOTIFICATION_GRANTED";

    private final RestTemplate restTemplate;

    private final KuFlowRestClient kuFlowRestClient;

    public SampleRestWorkerLoanController(RestTemplateBuilder restTemplateBuilder, KuFlowRestClient kuFlowRestClient) {
        this.restTemplate = restTemplateBuilder.setConnectTimeout(Duration.ofSeconds(500)).setReadTimeout(Duration.ofSeconds(500)).build();
        this.kuFlowRestClient = kuFlowRestClient;
    }

    @PostMapping
    public void handleEvent(@RequestBody String payload) {
        LOGGER.info("Event {}", payload);

        WebhookEvent event = this.kuFlowRestClient.parseWebhookEvent(payload);

        if (event instanceof WebhookEventProcessStateChanged) {
            this.handleEventProcessStateChanged(CastUtils.cast(event));
        } else if (event instanceof WebhookEventTaskStateChanged) {
            this.handleEventTaskStateChanged(CastUtils.cast(event));
        }
    }

    private void handleEventProcessStateChanged(WebhookEventProcessStateChanged event) {
        WebhookEventProcessStateChangedData data = event.getData();
        if (ProcessState.RUNNING.equals(data.getProcessState())) {
            this.createTaskLoanApplication(data);
        }
    }

    private void handleEventTaskStateChanged(WebhookEventTaskStateChanged event) {
        WebhookEventTaskStateChangedData data = event.getData();
        if (data.getTaskCode().equals(TASK_LOAN_APPLICATION) && TaskState.COMPLETED.equals(data.getTaskState())) {
            this.handleTaskLoanApplication(data);
        }
        if (data.getTaskCode().equals(TASK_APPROVE_LOAN) && TaskState.COMPLETED.equals(data.getTaskState())) {
            this.handleTaskApproveLoan(data);
        }
    }

    private void handleTaskApproveLoan(WebhookEventTaskStateChangedData data) {
        Task taskApproveLoan = this.kuFlowRestClient.getTaskOperations().retrieveTask(data.getTaskId());

        String authorizedField = taskApproveLoan.getElementValueAsString("authorized");

        Task taskNotification;
        if (authorizedField.equals("OK")) {
            taskNotification = this.createTaskNotificationGranted(data);
        } else {
            taskNotification = this.createTaskNotificationRejection(data);
        }

        Process process = this.kuFlowRestClient.getProcessOperations().retrieveProcess(data.getProcessId());

        this.assignTaskToProcessInitiator(taskNotification, process);

        this.kuFlowRestClient.getProcessOperations().actionsProcessComplete(data.getProcessId());
    }

    private void handleTaskLoanApplication(WebhookEventTaskStateChangedData data) {
        Task taskLoanApplication = this.kuFlowRestClient.getTaskOperations().retrieveTask(data.getTaskId());

        String currencyField = taskLoanApplication.getElementValueAsString("currency");
        String amountField = taskLoanApplication.getElementValueAsString("amount");

        BigDecimal amountEUR = this.convertToEuros(currencyField, amountField);

        if (amountEUR.compareTo(BigDecimal.valueOf(5000)) > 0) {
            this.createTaskApproveLoan(taskLoanApplication, amountEUR);
        } else {
            Task taskNotification = this.createTaskNotificationGranted(data);

            Process process = this.kuFlowRestClient.getProcessOperations().retrieveProcess(data.getProcessId());

            this.assignTaskToProcessInitiator(taskNotification, process);

            this.kuFlowRestClient.getProcessOperations().actionsProcessComplete(data.getProcessId());
        }
    }

    private void createTaskLoanApplication(WebhookEventProcessStateChangedData data) {
        TasksDefinitionSummary tasksDefinition = new TasksDefinitionSummary();
        tasksDefinition.setCode(TASK_LOAN_APPLICATION);

        Task task = new Task();
        task.setProcessId(data.getProcessId());
        task.setTaskDefinition(tasksDefinition);

        this.kuFlowRestClient.getTaskOperations().createTask(task);
    }

    private void createTaskApproveLoan(Task taskLoanApplication, BigDecimal amountEUR) {
        String firstName = taskLoanApplication.getElementValueAsString("firstName");
        String lastName = taskLoanApplication.getElementValueAsString("lastName");

        TasksDefinitionSummary tasksDefinition = new TasksDefinitionSummary();
        tasksDefinition.setCode(TASK_APPROVE_LOAN);

        Task taskApproveLoan = new Task();
        taskApproveLoan.setProcessId(taskLoanApplication.getProcessId());
        taskApproveLoan.setTaskDefinition(tasksDefinition);
        taskApproveLoan.setElementValueAsString("name", firstName + " " + lastName);
        taskApproveLoan.setElementValueAsString("amountRequested", amountEUR.toPlainString());

        this.kuFlowRestClient.getTaskOperations().createTask(taskApproveLoan);
    }

    private Task createTaskNotificationRejection(WebhookEventTaskStateChangedData data) {
        TasksDefinitionSummary tasksDefinition = new TasksDefinitionSummary();
        tasksDefinition.setCode(TASK_NOTIFICATION_REJECTION);

        Task taskNotificationRejection = new Task();
        taskNotificationRejection.setProcessId(data.getProcessId());
        taskNotificationRejection.setTaskDefinition(tasksDefinition);

        return this.kuFlowRestClient.getTaskOperations().createTask(taskNotificationRejection);
    }

    private Task createTaskNotificationGranted(WebhookEventTaskStateChangedData data) {
        TasksDefinitionSummary tasksDefinition = new TasksDefinitionSummary();
        tasksDefinition.setCode(TASK_NOTIFICATION_GRANTED);

        Task taskNotificationGranted = new Task();
        taskNotificationGranted.setProcessId(data.getProcessId());
        taskNotificationGranted.setTaskDefinition(tasksDefinition);

        return this.kuFlowRestClient.getTaskOperations().createTask(taskNotificationGranted);
    }

    private void assignTaskToProcessInitiator(Task taskNotification, Process process) {
        TaskAssignCommand command = new TaskAssignCommand();
        command.setPrincipalId(process.getInitiator().getId());

        this.kuFlowRestClient.getTaskOperations().actionsTaskAssign(taskNotification.getId(), command);
    }

    private BigDecimal convertToEuros(String currencyField, String amountField) {
        BigDecimal amountEUR = new BigDecimal(amountField != null ? amountField : "0");
        if (currencyField.equals("EUR")) {
            return amountEUR;
        } else {
            return this.convert(amountEUR, currencyField, "EUR");
        }
    }

    private BigDecimal convert(BigDecimal amount, String from, String to) {
        String fromTransformed = this.transformCurrencyCode(from);
        String toTransformed = this.transformCurrencyCode(to);
        String endpoint = String.format(
            "https://cdn.jsdelivr.net/gh/fawazahmed0/currency-api@1/latest/currencies/%s/%s.json",
            fromTransformed,
            toTransformed
        );

        ParameterizedTypeReference<HashMap<String, Object>> responseType = new ParameterizedTypeReference<>() {};
        RequestEntity<Void> request = RequestEntity.get(endpoint).build();
        HashMap<String, Object> response = this.restTemplate.exchange(request, responseType).getBody();

        Double conversion = (Double) response.get(toTransformed);

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
