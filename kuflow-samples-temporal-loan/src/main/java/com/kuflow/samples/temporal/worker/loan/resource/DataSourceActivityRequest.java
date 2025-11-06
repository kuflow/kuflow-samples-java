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

package com.kuflow.samples.temporal.worker.loan.resource;

import java.time.Duration;
import java.util.UUID;

/**
 * Abstract base class for data source activity requests.
 * Contains common properties shared by all data source operations (query, validation, etc.).
 */
public abstract class DataSourceActivityRequest {

    private UUID tenantId;

    private String code;

    private String invocationActivityName;

    private Duration invocationTimeout;

    private String invocationTaskQueue;

    public UUID getTenantId() {
        return this.tenantId;
    }

    public void setTenantId(UUID tenantId) {
        this.tenantId = tenantId;
    }

    public String getCode() {
        return this.code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getInvocationActivityName() {
        return this.invocationActivityName;
    }

    public void setInvocationActivityName(String invocationActivityName) {
        this.invocationActivityName = invocationActivityName;
    }

    public Duration getInvocationTimeout() {
        return this.invocationTimeout;
    }

    public void setInvocationTimeout(Duration invocationTimeout) {
        this.invocationTimeout = invocationTimeout;
    }

    public String getInvocationTaskQueue() {
        return this.invocationTaskQueue;
    }

    public void setInvocationTaskQueue(String invocationTaskQueue) {
        this.invocationTaskQueue = invocationTaskQueue;
    }
}
