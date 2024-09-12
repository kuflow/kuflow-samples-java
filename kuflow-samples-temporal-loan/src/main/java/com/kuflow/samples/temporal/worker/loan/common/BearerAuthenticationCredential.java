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
package com.kuflow.samples.temporal.worker.loan.common;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import java.time.OffsetDateTime;
import java.util.Objects;
import reactor.core.publisher.Mono;

/**
 * <p>The {@link BearerAuthenticationCredential} is used to authenticate and authorize requests made to
 * KuFlow services using the Bearer authentication scheme. Bearer Authentication is a simple authentication scheme
 * that uses token.</p>
 *
 * @see TokenCredential
 */
public record BearerAuthenticationCredential(String token) implements TokenCredential {
    /**
     * Creates a token authentication credential.
     *
     * @param token token
     */
    public BearerAuthenticationCredential(String token) {
        this.token = Objects.requireNonNull(token, "'token' is required");
    }

    /**
     * @throws RuntimeException If the UTF-8 encoding isn't supported.
     */
    @Override
    public Mono<AccessToken> getToken(TokenRequestContext request) {
        return Mono.fromCallable(() -> new AccessToken(this.token, OffsetDateTime.MAX));
    }
}
