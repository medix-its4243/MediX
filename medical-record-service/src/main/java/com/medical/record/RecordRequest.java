package com.medical.record;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record RecordRequest(@NotNull @Positive Long patientId, @NotNull @Positive Long doctorId,
                            @NotNull @Positive Long appointmentId, @NotBlank String diagnosis,
                            String prescription, String notes) {}
