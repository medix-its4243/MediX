package com.medical.auth.service;

import com.medical.auth.dto.TokenResponse;
import com.medical.auth.entity.AuthUser;
import com.medical.auth.repository.AuthUserRepository;
import java.time.Duration;
import java.time.Instant;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TokenService {
    private static final Duration ACCESS_LIFETIME = Duration.ofHours(1);
    private static final Duration REFRESH_LIFETIME = Duration.ofDays(7);
    private final JwtEncoder encoder;
    private final JwtDecoder refreshDecoder;
    private final AuthUserRepository users;
    private final String issuer;

    public TokenService(JwtEncoder encoder, SecretKey key, AuthUserRepository users,
                        @Value("${JWT_ISSUER:medix-auth}") String issuer) {
        this.encoder = encoder;
        this.users = users;
        this.issuer = issuer;
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(issuer));
        this.refreshDecoder = decoder;
    }

    public TokenResponse issue(AuthUser user) {
        return new TokenResponse(token(user, "access", ACCESS_LIFETIME),
                token(user, "refresh", REFRESH_LIFETIME), "Bearer", ACCESS_LIFETIME.toSeconds());
    }

    public TokenResponse refresh(String rawToken) {
        try {
            Jwt jwt = refreshDecoder.decode(rawToken);
            if (!"refresh".equals(jwt.getClaimAsString("type"))) throw new JwtException("Refresh token required");
            Long userId = Long.valueOf(jwt.getSubject());
            AuthUser user = users.findById(userId).orElseThrow(() -> new JwtException("User no longer exists"));
            if (!user.isEnabled()) throw new JwtException("User is disabled");
            return issue(user);
        } catch (JwtException | NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token", e);
        }
    }

    private String token(AuthUser user, String type, Duration lifetime) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder().issuer(issuer).subject(user.getId().toString())
                .issuedAt(now).expiresAt(now.plus(lifetime)).claim("email", user.getEmail())
                .claim("role", user.getRole().name()).claim("type", type).build();
        return encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
    }
}
