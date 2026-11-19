package com.medical.patient;

import java.time.LocalDate;

public record PatientResponse(Long id, Long userId, String name, LocalDate dob,
                              String gender, String phone, String address, String bloodGroup) {
    static PatientResponse from(Patient patient) {
        return new PatientResponse(patient.getId(), patient.getUserId(), patient.getName(),
                patient.getDob(), patient.getGender(), patient.getPhone(),
                patient.getAddress(), patient.getBloodGroup());
    }
}
