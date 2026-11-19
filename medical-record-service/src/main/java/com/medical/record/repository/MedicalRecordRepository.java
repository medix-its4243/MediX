package com.medical.record.repository;

import com.medical.record.entity.MedicalRecord;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {
    boolean existsByAppointmentId(Long appointmentId);
    List<MedicalRecord> findByPatientIdOrderByCreatedAtDesc(Long patientId);
}
