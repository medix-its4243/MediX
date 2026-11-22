package com.medical.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.medical.auth.dto.LoginRequest;
import com.medical.auth.dto.RegisterRequest;
import com.medical.auth.entity.AuthUser;
import com.medical.auth.entity.Role;
import com.medical.auth.repository.AuthUserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock AuthUserRepository users;
    @Mock TokenService tokens;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Test
    void publicRegistrationAlwaysCreatesPatientWithHashedPassword() {
        when(users.save(any(AuthUser.class))).thenAnswer(call -> call.getArgument(0));
        AuthService service = new AuthService(users, encoder, tokens);
        var result = service.register(new RegisterRequest("  Ada@Example.com ", "strong-password"));
        assertEquals(Role.PATIENT, result.role());
        assertEquals("ada@example.com", result.email());
        org.mockito.ArgumentCaptor<AuthUser> captor = org.mockito.ArgumentCaptor.forClass(AuthUser.class);
        org.mockito.Mockito.verify(users).save(captor.capture());
        assertNotEquals("strong-password", captor.getValue().getPasswordHash());
        org.junit.jupiter.api.Assertions.assertTrue(encoder.matches("strong-password", captor.getValue().getPasswordHash()));
    }

    @Test
    void loginRejectsIncorrectPassword() {
        AuthUser user = new AuthUser();
        user.setEmail("ada@example.com");
        user.setPasswordHash(encoder.encode("correct-password"));
        when(users.findByEmailIgnoreCase("ada@example.com")).thenReturn(Optional.of(user));
        AuthService service = new AuthService(users, encoder, tokens);
        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.login(new LoginRequest("ada@example.com", "wrong-password")));
        assertEquals(HttpStatus.UNAUTHORIZED, error.getStatusCode());
    }
}
