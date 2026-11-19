package com.medical.patient.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

public record PatientRequest(
        @NotNull @Positive Long userId,
        @NotBlank String name,
        @Past LocalDate dob,
        String gender,
        String phone,
        String address,
        String bloodGroup) {}
