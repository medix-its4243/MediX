package com.medical.billing.dto;
import jakarta.validation.constraints.*;
public record InvoiceRequest(@NotNull @Positive Long appointmentId) {}
