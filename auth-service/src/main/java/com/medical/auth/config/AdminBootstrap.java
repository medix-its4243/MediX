package com.medical.auth.config;

import com.medical.auth.entity.AuthUser;
import com.medical.auth.entity.Role;
import com.medical.auth.repository.AuthUserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminBootstrap {
    @Bean
    CommandLineRunner seedAdmin(AuthUserRepository users, PasswordEncoder passwords,
                                @Value("${BOOTSTRAP_ADMIN_EMAIL:}") String email,
                                @Value("${BOOTSTRAP_ADMIN_PASSWORD:}") String password) {
        return args -> {
            if (email.isBlank() && password.isBlank()) return;
            if (email.isBlank() || password.length() < 8)
                throw new IllegalArgumentException("Set BOOTSTRAP_ADMIN_EMAIL and a password of at least 8 characters");
            if (users.existsByEmailIgnoreCase(email.trim())) return;
            AuthUser admin = new AuthUser();
            admin.setEmail(email.trim().toLowerCase(java.util.Locale.ROOT));
            admin.setPasswordHash(passwords.encode(password));
            admin.setRole(Role.ADMIN);
            users.save(admin);
        };
    }
}
