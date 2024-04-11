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
package com.kuflow.samples.temporal.worker.loan.activity;

import io.temporal.failure.ApplicationFailure;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class CurrencyConversionActivitiesImpl implements CurrencyConversionActivities {

    private final RestTemplate restTemplate;

    public CurrencyConversionActivitiesImpl(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.setConnectTimeout(Duration.ofSeconds(500)).setReadTimeout(Duration.ofSeconds(500)).build();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public String convert(String amountText, String from, String to) {
        BigDecimal amount = new BigDecimal(amountText);

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
