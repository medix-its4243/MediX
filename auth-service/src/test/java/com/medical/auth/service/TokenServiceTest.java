package com.medical.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.medical.auth.config.AuthSecurityConfig;
import com.medical.auth.entity.AuthUser;
import com.medical.auth.entity.Role;
import com.medical.auth.repository.AuthUserRepository;
import java.util.Optional;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.test.util.ReflectionTestUtils;

class TokenServiceTest {
    private final AuthSecurityConfig config = new AuthSecurityConfig();
    private final SecretKey key = config.jwtKey("test-only-32-byte-secret-key-for-jwt-signing");

    @Test
    void accessDecoderRejectsRefreshTokenAndRefreshIssuesNewPair() {
        AuthUser user = new AuthUser();
        ReflectionTestUtils.setField(user, "id", 7L);
        user.setEmail("doctor@example.com");
        user.setRole(Role.DOCTOR);
        AuthUserRepository users = Mockito.mock(AuthUserRepository.class);
        when(users.findById(7L)).thenReturn(Optional.of(user));
        TokenService tokens = new TokenService(config.jwtEncoder(key), key, users, "medix-auth");
        var pair = tokens.issue(user);
        var access = config.jwtDecoder(key, "medix-auth").decode(pair.accessToken());
        assertEquals("7", access.getSubject());
        assertEquals("DOCTOR", access.getClaimAsString("role"));
        assertThrows(JwtException.class, () -> config.jwtDecoder(key, "medix-auth").decode(pair.refreshToken()));
        assertEquals("access", config.jwtDecoder(key, "medix-auth")
                .decode(tokens.refresh(pair.refreshToken()).accessToken()).getClaimAsString("type"));
    }
}
