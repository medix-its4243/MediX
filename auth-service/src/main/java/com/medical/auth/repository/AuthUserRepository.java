package com.medical.auth.repository;

import com.medical.auth.entity.AuthUser;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthUserRepository extends JpaRepository<AuthUser, Long> {
    Optional<AuthUser> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
}
