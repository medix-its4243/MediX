package com.medical.gateway.config;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class VerifiedIdentityFilter implements GlobalFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return exchange.getPrincipal().ofType(JwtAuthenticationToken.class)
                .map(auth -> withIdentity(exchange, auth))
                .defaultIfEmpty(withIdentity(exchange, null))
                .flatMap(chain::filter);
    }

    private ServerWebExchange withIdentity(ServerWebExchange exchange, JwtAuthenticationToken auth) {
        var request = exchange.getRequest().mutate().headers(headers -> {
            headers.remove("X-User-Id");
            headers.remove("X-User-Role");
            if (auth != null) {
                headers.set("X-User-Id", auth.getToken().getSubject());
                headers.set("X-User-Role", auth.getToken().getClaimAsString("role"));
            }
        }).build();
        return exchange.mutate().request(request).build();
    }
}
