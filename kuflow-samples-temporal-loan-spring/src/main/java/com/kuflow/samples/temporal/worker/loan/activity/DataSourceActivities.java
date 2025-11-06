/*
 * Copyright (c) 2021-present KuFlow S.L.
 *
 * All rights reserved.
 */

package com.kuflow.samples.temporal.worker.loan.activity;

import com.kuflow.samples.temporal.worker.loan.resource.DataSourceQueryRequest;
import com.kuflow.samples.temporal.worker.loan.resource.DataSourceQueryResponse;
import com.kuflow.samples.temporal.worker.loan.resource.DataSourceValidateValueRequest;
import com.kuflow.samples.temporal.worker.loan.resource.DataSourceValidateValueResponse;
import io.temporal.activity.ActivityInterface;
import io.temporal.workflow.WorkflowMethod;

@ActivityInterface(namePrefix = "DataSource_")
public interface DataSourceActivities {
    @WorkflowMethod
    DataSourceQueryResponse runQuery(DataSourceQueryRequest request);

    @WorkflowMethod
    DataSourceValidateValueResponse validateValue(DataSourceValidateValueRequest request);
}
