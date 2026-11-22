package com.medical.auth.dto;

import com.medical.auth.entity.AuthUser;
import com.medical.auth.entity.Role;

public record UserResponse(Long id, String email, Role role) {
    public static UserResponse from(AuthUser user) { return new UserResponse(user.getId(), user.getEmail(), user.getRole()); }
}
