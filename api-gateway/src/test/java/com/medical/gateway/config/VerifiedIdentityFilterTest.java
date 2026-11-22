package com.medical.gateway.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import reactor.core.publisher.Mono;

class VerifiedIdentityFilterTest {
    private final VerifiedIdentityFilter filter = new VerifiedIdentityFilter();

    @Test
    void replacesSpoofedHeadersWithVerifiedClaims() {
        Jwt jwt = new Jwt("token", Instant.now(), Instant.now().plusSeconds(60),
                Map.of("alg", "HS256"), Map.of("sub", "42", "role", "PATIENT"));
        var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/patients/1")
                .header("X-User-Id", "999").header("X-User-Role", "ADMIN"));
        var authenticated = exchange.mutate().principal(Mono.just(new JwtAuthenticationToken(jwt))).build();
        filter.filter(authenticated, forwarded -> {
            assertEquals("42", forwarded.getRequest().getHeaders().getFirst("X-User-Id"));
            assertEquals("PATIENT", forwarded.getRequest().getHeaders().getFirst("X-User-Role"));
            return Mono.empty();
        }).block();
    }

    @Test
    void removesIdentityHeadersFromPublicRequests() {
        var exchange = MockServerWebExchange.from(MockServerHttpRequest.post("/auth/login")
                .header("X-User-Role", "ADMIN").header("X-User-Id", "42"));
        filter.filter(exchange, forwarded -> {
            assertNull(forwarded.getRequest().getHeaders().getFirst("X-User-Id"));
            assertNull(forwarded.getRequest().getHeaders().getFirst("X-User-Role"));
            return Mono.empty();
        }).block();
    }
}
