package com.medical.appointment.repository;

import com.medical.appointment.entity.Appointment;

import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByPatientIdOrderByDateDescStartTimeDesc(Long patientId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Appointment a where a.id = :id")
    Optional<Appointment> findForUpdate(@Param("id") Long id);

    @Query("select count(a) > 0 from Appointment a where a.doctorId = :doctorId and a.date = :date " +
            "and a.status = com.medical.appointment.entity.AppointmentStatus.BOOKED " +
            "and a.startTime < :endTime and a.endTime > :startTime")
    boolean hasOverlap(@Param("doctorId") Long doctorId, @Param("date") LocalDate date,
                       @Param("startTime") LocalTime startTime, @Param("endTime") LocalTime endTime);
}
