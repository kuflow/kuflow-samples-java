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
package com.kuflow.samples.temporal.worker.loan;

public class SampleEngineWorkerLoanProperties {

    private KuFlowProperties kuflow = new KuFlowProperties();

    private TemporalProperties temporal = new TemporalProperties();

    public KuFlowProperties getKuflow() {
        return this.kuflow;
    }

    public void setKuflow(KuFlowProperties kuflow) {
        this.kuflow = kuflow;
    }

    public TemporalProperties getTemporal() {
        return this.temporal;
    }

    public void setTemporal(TemporalProperties temporal) {
        this.temporal = temporal;
    }

    public static final class KuFlowProperties {

        private KuFlowApiProperties api = new KuFlowApiProperties();

        public KuFlowApiProperties getApi() {
            return this.api;
        }

        public void setApi(KuFlowApiProperties api) {
            this.api = api;
        }
    }

    public static final class KuFlowApiProperties {

        private String endpoint;

        private String clientId;

        private String clientSecret;

        public String getEndpoint() {
            return this.endpoint;
        }

        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }

        public String getClientId() {
            return this.clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public String getClientSecret() {
            return this.clientSecret;
        }

        public void setClientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
        }
    }

    public static final class TemporalProperties {

        private String namespace;

        private String kuflowQueue;

        private String target;

        private MutualTlsProperties mutualTls = new MutualTlsProperties();

        public MutualTlsProperties getMutualTls() {
            return this.mutualTls;
        }

        public void setMutualTls(MutualTlsProperties mutualTls) {
            this.mutualTls = mutualTls;
        }

        public String getNamespace() {
            return this.namespace;
        }

        public void setNamespace(String namespace) {
            this.namespace = namespace;
        }

        public String getTarget() {
            return this.target;
        }

        public void setTarget(String target) {
            this.target = target;
        }

        public String getKuflowQueue() {
            return this.kuflowQueue;
        }

        public void setKuflowQueue(String kuflowQueue) {
            this.kuflowQueue = kuflowQueue;
        }

        public static final class MutualTlsProperties {

            private String caFile;

            private String caData;

            private String certFile;

            private String certData;

            private String key;

            private String keyData;

            public String getCaFile() {
                return this.caFile;
            }

            public void setCaFile(String caFile) {
                this.caFile = caFile;
            }

            public String getCaData() {
                return this.caData;
            }

            public void setCaData(String caData) {
                this.caData = caData;
            }

            public String getCertFile() {
                return this.certFile;
            }

            public void setCertFile(String certFile) {
                this.certFile = certFile;
            }

            public String getCertData() {
                return this.certData;
            }

            public void setCertData(String certData) {
                this.certData = certData;
            }

            public String getKey() {
                return this.key;
            }

            public void setKey(String key) {
                this.key = key;
            }

            public String getKeyData() {
                return this.keyData;
            }

            public void setKeyData(String keyData) {
                this.keyData = keyData;
            }
        }
    }
}