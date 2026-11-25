package com.medical.doctor.dto;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
public record DoctorRequest(@NotNull @Positive Long userId, @NotBlank @Size(max=255) String name,
 @NotBlank @Size(max=255) String specialization, @Size(max=255) String phone,
 @NotNull @DecimalMin("0.00") @Digits(integer=10, fraction=2) BigDecimal consultationFee,
 @NotBlank @Size(max=255) String availableDays) {}
