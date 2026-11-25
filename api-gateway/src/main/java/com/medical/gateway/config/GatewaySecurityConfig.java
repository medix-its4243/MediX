package com.medical.gateway.config;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import reactor.core.publisher.Flux;

@Configuration
public class GatewaySecurityConfig {
    @Bean
    ReactiveJwtDecoder jwtDecoder(@Value("${JWT_SECRET}") String secret,
                                  @Value("${JWT_ISSUER:medix-auth}") String issuer) {
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) throw new IllegalArgumentException("JWT_SECRET must contain at least 32 UTF-8 bytes");
        SecretKey key = new SecretKeySpec(bytes, "HmacSHA256");
        NimbusReactiveJwtDecoder decoder = NimbusReactiveJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
        OAuth2TokenValidator<Jwt> access = token -> "access".equals(token.getClaimAsString("type"))
                ? OAuth2TokenValidatorResult.success()
                : OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "Access token required", null));
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(JwtValidators.createDefaultWithIssuer(issuer), access));
        return decoder;
    }

    @Bean
    SecurityWebFilterChain security(ServerHttpSecurity http, ReactiveJwtDecoder decoder,
                                    @Value("${ALLOWED_ORIGINS:http://localhost:3000,http://localhost:5173}") String origins) {
        CorsConfiguration cors = new CorsConfiguration();
        cors.setAllowedOrigins(Arrays.stream(origins.split(",")).map(String::trim).toList());
        cors.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        cors.setAllowedHeaders(java.util.List.of("Authorization", "Content-Type"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cors);
        ReactiveJwtAuthenticationConverter converter = new ReactiveJwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> Flux.<GrantedAuthority>just(
                new SimpleGrantedAuthority("ROLE_" + jwt.getClaimAsString("role"))));
        return http.csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(c -> c.configurationSource(source))
                .authorizeExchange(auth -> auth
                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .pathMatchers("/actuator/health", "/actuator/info").permitAll()
                        .pathMatchers(HttpMethod.POST, "/auth/register", "/auth/login", "/auth/refresh").permitAll()
                        .pathMatchers(HttpMethod.POST, "/auth/users").hasRole("ADMIN")
                        .pathMatchers("/auth/me").authenticated()
                        .pathMatchers(HttpMethod.POST, "/doctors").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.GET, "/doctors", "/doctors/**").authenticated()
                        .pathMatchers("/patients", "/patients/**").hasAnyRole("ADMIN", "STAFF", "PATIENT")
                        .pathMatchers(HttpMethod.POST, "/appointments").hasAnyRole("PATIENT", "STAFF")
                        .pathMatchers(HttpMethod.PUT, "/appointments/*/complete").hasRole("DOCTOR")
                        .pathMatchers(HttpMethod.PUT, "/appointments/*/cancel").hasAnyRole("PATIENT", "STAFF")
                        .pathMatchers(HttpMethod.GET, "/appointments/**").authenticated()
                        .pathMatchers(HttpMethod.POST, "/records").hasRole("DOCTOR")
                        .pathMatchers(HttpMethod.GET, "/records/**").hasAnyRole("DOCTOR", "ADMIN", "PATIENT")
                        .pathMatchers(HttpMethod.GET, "/invoices/**").hasAnyRole("ADMIN", "STAFF", "PATIENT")
                        .pathMatchers(HttpMethod.POST, "/invoices", "/invoices/*/pay").hasAnyRole("ADMIN", "STAFF")
                        .anyExchange().denyAll())
                .oauth2ResourceServer(oauth -> oauth.jwt(jwt -> jwt.jwtDecoder(decoder).jwtAuthenticationConverter(converter)))
                .build();
    }
}
