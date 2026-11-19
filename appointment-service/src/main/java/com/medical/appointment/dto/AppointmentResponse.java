package com.medical.appointment.dto;

import com.medical.appointment.entity.Appointment;
import com.medical.appointment.entity.AppointmentStatus;

import java.time.LocalDate;
import java.time.LocalTime;

public record AppointmentResponse(Long id, Long patientId, Long doctorId, LocalDate date,
                                  LocalTime startTime, LocalTime endTime, AppointmentStatus status, String reason) {
    public static AppointmentResponse from(Appointment appointment) {
        return new AppointmentResponse(appointment.getId(), appointment.getPatientId(), appointment.getDoctorId(),
                appointment.getDate(), appointment.getStartTime(), appointment.getEndTime(),
                appointment.getStatus(), appointment.getReason());
    }
}
