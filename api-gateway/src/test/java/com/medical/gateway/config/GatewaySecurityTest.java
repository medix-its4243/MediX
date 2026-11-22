package com.medical.gateway.config;

import static org.springframework.security.oauth2.jose.jws.MacAlgorithm.HS256;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"JWT_SECRET=test-only-32-byte-secret-key-for-jwt-signing",
                "eureka.client.enabled=false", "spring.cloud.discovery.enabled=false"})
@AutoConfigureWebTestClient
class GatewaySecurityTest {
    @Autowired WebTestClient web;

    @Test
    void rejectsUnauthenticatedPatientRequest() {
        web.get().uri("/patients/1").exchange().expectStatus().isUnauthorized();
    }

    @Test
    void rejectsPatientTryingToCreateDoctor() {
        web.post().uri("/doctors").header("Authorization", "Bearer " + token("PATIENT", "access"))
                .exchange().expectStatus().isForbidden();
    }

    @Test
    void rejectsRefreshTokenForProtectedRoute() {
        web.get().uri("/patients/1").header("Authorization", "Bearer " + token("PATIENT", "refresh"))
                .exchange().expectStatus().isUnauthorized();
    }

    private String token(String role, String type) {
        byte[] bytes = "test-only-32-byte-secret-key-for-jwt-signing".getBytes(StandardCharsets.UTF_8);
        var encoder = new NimbusJwtEncoder(new ImmutableSecret<>(new SecretKeySpec(bytes, "HmacSHA256")));
        Instant now = Instant.now();
        var claims = JwtClaimsSet.builder().issuer("medix-auth").subject("1").issuedAt(now)
                .expiresAt(now.plusSeconds(3600)).claim("role", role).claim("type", type).build();
        return encoder.encode(JwtEncoderParameters.from(JwsHeader.with(HS256).build(), claims)).getTokenValue();
    }
}
