package com.medical.patient.repository;

import com.medical.patient.entity.Patient;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientRepository extends JpaRepository<Patient, Long> {
    boolean existsByUserId(Long userId);
    Optional<Patient> findByUserId(Long userId);
}
