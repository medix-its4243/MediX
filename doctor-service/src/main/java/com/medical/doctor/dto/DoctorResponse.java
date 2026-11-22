package com.medical.doctor.dto;

import com.medical.doctor.entity.Doctor;
import java.math.BigDecimal;

public record DoctorResponse(Long id, Long userId, String name, String specialization,
                             String phone, BigDecimal consultationFee, String availableDays) {
    public static DoctorResponse from(Doctor doctor) {
        return new DoctorResponse(doctor.getId(), doctor.getUserId(), doctor.getName(),
                doctor.getSpecialization(), doctor.getPhone(), doctor.getConsultationFee(),
                doctor.getAvailableDays());
    }
}
