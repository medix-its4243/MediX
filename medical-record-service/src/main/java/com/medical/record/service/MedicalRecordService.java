package com.medical.record.service;

import com.medical.record.client.AppointmentClient;
import com.medical.record.client.PatientClient;
import com.medical.record.dto.RecordRequest;
import com.medical.record.dto.RecordResponse;
import com.medical.record.entity.MedicalRecord;
import com.medical.record.repository.MedicalRecordRepository;

import feign.FeignException;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class MedicalRecordService {
    private final MedicalRecordRepository repository;
    private final PatientClient patients;
    private final AppointmentClient appointments;

    public MedicalRecordService(MedicalRecordRepository repository, PatientClient patients,
                                AppointmentClient appointments) {
        this.repository = repository;
        this.patients = patients;
        this.appointments = appointments;
    }

    @Transactional
    public RecordResponse create(RecordRequest request, String userRole) {
        if (!"DOCTOR".equals(userRole)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only doctors can create medical records");
        }
        try {
            if (patients.get(request.patientId()) == null) throw invalidReference();
            AppointmentClient.AppointmentRef appointment = appointments.get(request.appointmentId());
            if (appointment == null || !request.patientId().equals(appointment.patientId())
                    || !request.doctorId().equals(appointment.doctorId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Record does not match the appointment");
            }
            if (!"COMPLETED".equals(appointment.status())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Appointment must be COMPLETED");
            }
        } catch (FeignException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Patient or appointment does not exist", e);
        } catch (FeignException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Reference service unavailable", e);
        }
        if (repository.existsByAppointmentId(request.appointmentId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Medical record already exists for appointment");
        }
        MedicalRecord record = new MedicalRecord();
        record.setPatientId(request.patientId());
        record.setDoctorId(request.doctorId());
        record.setAppointmentId(request.appointmentId());
        record.setDiagnosis(request.diagnosis().trim());
        record.setPrescription(request.prescription());
        record.setNotes(request.notes());
        return RecordResponse.from(repository.save(record));
    }

    @Transactional(readOnly = true)
    public RecordResponse get(Long id, String userRole, Long userId) {
        MedicalRecord record = repository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Medical record not found with id " + id));
        checkRead(record.getPatientId(), userRole, userId);
        return RecordResponse.from(record);
    }

    @Transactional(readOnly = true)
    public List<RecordResponse> forPatient(Long patientId, String userRole, Long userId) {
        checkRead(patientId, userRole, userId);
        return repository.findByPatientIdOrderByCreatedAtDesc(patientId).stream()
                .map(RecordResponse::from).toList();
    }

    private void checkRead(Long patientId, String userRole, Long userId) {
        if ("DOCTOR".equals(userRole) || "ADMIN".equals(userRole)) return;
        if ("PATIENT".equals(userRole) && userId != null) {
            try {
                PatientClient.PatientRef patient = patients.get(patientId);
                if (patient != null && userId.equals(patient.userId())) return;
            } catch (FeignException.NotFound e) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found with id " + patientId, e);
            } catch (FeignException e) {
                throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Patient service unavailable", e);
            }
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access to this medical record is forbidden");
    }

    private static ResponseStatusException invalidReference() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Patient does not exist");
    }
}
