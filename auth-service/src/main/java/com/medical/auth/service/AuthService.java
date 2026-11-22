package com.medical.auth.service;

import com.medical.auth.dto.CreateUserRequest;
import com.medical.auth.dto.LoginRequest;
import com.medical.auth.dto.RegisterRequest;
import com.medical.auth.dto.TokenResponse;
import com.medical.auth.dto.UserResponse;
import com.medical.auth.entity.AuthUser;
import com.medical.auth.entity.Role;
import com.medical.auth.repository.AuthUserRepository;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {
    private final AuthUserRepository users;
    private final PasswordEncoder passwords;
    private final TokenService tokens;

    public AuthService(AuthUserRepository users, PasswordEncoder passwords, TokenService tokens) {
        this.users = users;
        this.passwords = passwords;
        this.tokens = tokens;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        return create(request.email(), request.password(), Role.PATIENT);
    }

    @Transactional
    public UserResponse createByAdmin(CreateUserRequest request) {
        return create(request.email(), request.password(), request.role());
    }

    private UserResponse create(String email, String password, Role role) {
        String normalized = email.trim().toLowerCase(Locale.ROOT);
        if (users.existsByEmailIgnoreCase(normalized))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already registered");
        AuthUser user = new AuthUser();
        user.setEmail(normalized);
        user.setPasswordHash(passwords.encode(password));
        user.setRole(role);
        return UserResponse.from(users.save(user));
    }

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        AuthUser user = users.findByEmailIgnoreCase(request.email().trim()).orElseThrow(AuthService::invalidCredentials);
        if (!user.isEnabled() || !passwords.matches(request.password(), user.getPasswordHash()))
            throw invalidCredentials();
        return tokens.issue(user);
    }

    @Transactional(readOnly = true)
    public UserResponse me(Long id) {
        AuthUser user = users.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        if (!user.isEnabled()) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is disabled");
        return UserResponse.from(user);
    }

    private static ResponseStatusException invalidCredentials() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
    }
}
