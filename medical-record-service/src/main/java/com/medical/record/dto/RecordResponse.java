package com.medical.record.dto;

import com.medical.record.entity.MedicalRecord;

import java.time.LocalDateTime;

public record RecordResponse(Long id, Long patientId, Long doctorId, Long appointmentId,
                             String diagnosis, String prescription, String notes, LocalDateTime createdAt) {
    public static RecordResponse from(MedicalRecord record) {
        return new RecordResponse(record.getId(), record.getPatientId(), record.getDoctorId(),
                record.getAppointmentId(), record.getDiagnosis(), record.getPrescription(),
                record.getNotes(), record.getCreatedAt());
    }
}
