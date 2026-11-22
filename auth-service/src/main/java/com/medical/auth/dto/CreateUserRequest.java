package com.medical.auth.dto;

import com.medical.auth.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(@NotBlank @Email String email, @NotBlank @Size(min = 8) String password,
                                @NotNull Role role) {}
