package com.medical.doctor.repository;

import com.medical.doctor.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {
 java.util.List<Doctor> findBySpecializationContainingIgnoreCase(String specialization);
}
