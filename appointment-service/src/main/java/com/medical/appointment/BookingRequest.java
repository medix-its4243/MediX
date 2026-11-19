package com.medical.appointment;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import java.time.LocalTime;

public record BookingRequest(@NotNull @Positive Long patientId, @NotNull @Positive Long doctorId,
                             @NotNull @FutureOrPresent LocalDate date, @NotNull LocalTime startTime,
                             @NotNull LocalTime endTime, String reason) {}
