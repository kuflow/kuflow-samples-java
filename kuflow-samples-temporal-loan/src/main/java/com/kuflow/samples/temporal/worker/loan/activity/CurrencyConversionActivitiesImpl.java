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

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import io.temporal.failure.ApplicationFailure;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Scanner;

public class CurrencyConversionActivitiesImpl implements CurrencyConversionActivities {

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

        try (Scanner scanner = new Scanner(new URL(endpoint).openStream(), StandardCharsets.UTF_8).useDelimiter("\\A")) {
            String json = scanner.next();

            Type mapType = new TypeToken<Map<String, Object>>() {}.getType();
            Gson gson = new Gson();
            Map<String, Object> response = gson.fromJson(json, mapType);

            Double conversion = (Double) response.get(toTransformed);

            return amount.multiply(BigDecimal.valueOf(conversion)).toPlainString();
        } catch (Exception e) {
            throw ApplicationFailure.newNonRetryableFailure(e.getMessage(), "CurrencyConversionActivities.error");
        }
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
