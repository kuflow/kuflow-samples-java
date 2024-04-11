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
        return Mono.fromCallable(() -> new AccessToken(token, OffsetDateTime.MAX));
    }
}
