/*
 * Copyright (c) 2021-present KuFlow S.L.
 *
 * All rights reserved.
 */

package com.kuflow.engine.samples.worker.loan.activity;

public class CurrencyConversionActivitiesDelegate implements CurrencyConversionActivities {

    private final CurrencyConversionActivities delegate;

    public CurrencyConversionActivitiesDelegate(CurrencyConversionActivities delegate) {
        this.delegate = delegate;
    }

    @Override
    public String convert(String amountText, String from, String to) {
        return this.delegate.convert(amountText, from, to);
    }
}
