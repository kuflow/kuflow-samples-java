/*
 * Copyright (c) 2021-present KuFlow S.L.
 *
 * All rights reserved.
 */

package com.kuflow.engine.samples.worker.loan.activity;

import io.temporal.failure.ApplicationFailure;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.HashMap;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class CurrencyConversionActivitiesFacade implements CurrencyConversionActivities {

    private final RestTemplate restTemplate;

    public CurrencyConversionActivitiesFacade(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.setConnectTimeout(Duration.ofSeconds(500)).setReadTimeout(Duration.ofSeconds(500)).build();
    }

    @Override
    public String convert(String amountText, String from, String to) {
        BigDecimal amount = new BigDecimal(amountText);

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

        return amount.multiply(BigDecimal.valueOf(conversion)).toPlainString();
    }

    private String transformCurrencyCode(String currency) {
        return switch (currency) {
            case "EUR" -> "eur";
            case "USD" -> "usd";
            case "GBP" -> "gbp";
            default -> throw ApplicationFailure.newNonRetryableFailure("Unsupported currency " + currency, "CurrencyConversion");
        };
    }
}
