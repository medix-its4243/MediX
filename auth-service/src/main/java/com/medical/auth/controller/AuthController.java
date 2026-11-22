package com.medical.auth.controller;

import com.medical.auth.dto.CreateUserRequest;
import com.medical.auth.dto.LoginRequest;
import com.medical.auth.dto.RefreshRequest;
import com.medical.auth.dto.RegisterRequest;
import com.medical.auth.dto.TokenResponse;
import com.medical.auth.dto.UserResponse;
import com.medical.auth.service.AuthService;
import com.medical.auth.service.TokenService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService auth;
    private final TokenService tokens;
    public AuthController(AuthService auth, TokenService tokens) { this.auth = auth; this.tokens = tokens; }

    @PostMapping("/register") @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody RegisterRequest request) { return auth.register(request); }
    @PostMapping("/users") @ResponseStatus(HttpStatus.CREATED)
    public UserResponse createByAdmin(@Valid @RequestBody CreateUserRequest request) { return auth.createByAdmin(request); }
    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) { return auth.login(request); }
    @PostMapping("/refresh")
    public TokenResponse refresh(@Valid @RequestBody RefreshRequest request) { return tokens.refresh(request.refreshToken()); }
    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal Jwt jwt) { return auth.me(Long.valueOf(jwt.getSubject())); }
}
