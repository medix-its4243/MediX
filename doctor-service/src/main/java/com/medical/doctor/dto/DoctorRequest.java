package com.medical.doctor.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record DoctorRequest(@NotNull @Positive Long userId, @NotBlank String name,
                            String specialization, String phone, BigDecimal consultationFee,
                            @NotBlank String availableDays) {}
