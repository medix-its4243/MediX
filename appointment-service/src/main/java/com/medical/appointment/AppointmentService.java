package com.medical.appointment;

import feign.FeignException;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AppointmentService {
    private final AppointmentRepository repository;
    private final PatientClient patients;
    private final DoctorClient doctors;
    private final EntityManager entityManager;

    public AppointmentService(AppointmentRepository repository, PatientClient patients,
                              DoctorClient doctors, EntityManager entityManager) {
        this.repository = repository;
        this.patients = patients;
        this.doctors = doctors;
        this.entityManager = entityManager;
    }

    @Transactional
    public AppointmentResponse book(BookingRequest request) {
        if (!request.startTime().isBefore(request.endTime())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "startTime must be before endTime");
        }
        try {
            if (patients.get(request.patientId()) == null) throw invalidReference("Patient");
            DoctorClient.DoctorRef doctor = doctors.get(request.doctorId());
            if (doctor == null) throw invalidReference("Doctor");
            String day = request.date().getDayOfWeek().name().substring(0, 3);
            if (doctor.availableDays() == null ||
                    !List.of(doctor.availableDays().toUpperCase(Locale.ROOT).split(",")).stream()
                            .map(String::trim).anyMatch(day::equals)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Doctor is unavailable on this day");
            }
        } catch (FeignException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Patient or doctor does not exist", e);
        } catch (FeignException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Reference service unavailable", e);
        }
        // Serialize bookings for one doctor on one day, including when no appointments exist yet.
        long lockKey = Long.rotateLeft(request.doctorId(), 32) ^ request.date().toEpochDay();
        entityManager.createNativeQuery("SELECT pg_advisory_xact_lock(:key)")
                .setParameter("key", lockKey).getSingleResult();
        if (repository.hasOverlap(request.doctorId(), request.date(), request.startTime(), request.endTime())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Doctor already has a booked appointment in this time slot");
        }
        Appointment appointment = new Appointment();
        appointment.setPatientId(request.patientId());
        appointment.setDoctorId(request.doctorId());
        appointment.setDate(request.date());
        appointment.setStartTime(request.startTime());
        appointment.setEndTime(request.endTime());
        appointment.setReason(request.reason());
        appointment.setStatus(AppointmentStatus.BOOKED);
        return AppointmentResponse.from(repository.save(appointment));
    }

    @Transactional(readOnly = true)
    public AppointmentResponse get(Long id) { return AppointmentResponse.from(find(id)); }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> forPatient(Long patientId) {
        return repository.findByPatientIdOrderByDateDescStartTimeDesc(patientId).stream()
                .map(AppointmentResponse::from).toList();
    }

    @Transactional
    public AppointmentResponse cancel(Long id) { return changeStatus(id, AppointmentStatus.CANCELLED); }

    @Transactional
    public AppointmentResponse complete(Long id) { return changeStatus(id, AppointmentStatus.COMPLETED); }

    private AppointmentResponse changeStatus(Long id, AppointmentStatus status) {
        Appointment appointment = repository.findForUpdate(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found with id " + id));
        if (appointment.getStatus() != AppointmentStatus.BOOKED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only BOOKED appointments can change status");
        }
        appointment.setStatus(status);
        return AppointmentResponse.from(repository.save(appointment));
    }

    private Appointment find(Long id) {
        return repository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found with id " + id));
    }

    private static ResponseStatusException invalidReference(String name) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, name + " does not exist");
    }
}
