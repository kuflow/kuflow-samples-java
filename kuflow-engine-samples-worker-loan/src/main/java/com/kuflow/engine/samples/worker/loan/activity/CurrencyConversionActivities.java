/*
 * Copyright (c) 2021-present KuFlow S.L.
 *
 * All rights reserved.
 */

package com.kuflow.engine.samples.worker.loan.activity;

import io.temporal.activity.ActivityInterface;

@ActivityInterface
public interface CurrencyConversionActivities {
    String convert(String amountText, String from, String to);
}
