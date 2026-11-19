package com.medical.patient.service;

import com.medical.patient.dto.PatientRequest;
import com.medical.patient.dto.PatientResponse;
import com.medical.patient.entity.Patient;
import com.medical.patient.repository.PatientRepository;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PatientService {
    private final PatientRepository repository;

    public PatientService(PatientRepository repository) { this.repository = repository; }

    @Transactional
    public PatientResponse create(PatientRequest request) {
        if (repository.existsByUserId(request.userId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Patient already exists for user " + request.userId());
        }
        Patient patient = new Patient();
        apply(patient, request);
        return PatientResponse.from(repository.save(patient));
    }

    @Transactional(readOnly = true)
    public List<PatientResponse> list() {
        return repository.findAll().stream().map(PatientResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public PatientResponse get(Long id) { return PatientResponse.from(find(id)); }

    @Transactional
    public PatientResponse update(Long id, PatientRequest request) {
        Patient patient = find(id);
        if (!patient.getUserId().equals(request.userId()) && repository.existsByUserId(request.userId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Patient already exists for user " + request.userId());
        }
        apply(patient, request);
        return PatientResponse.from(repository.save(patient));
    }

    @Transactional
    public void delete(Long id) { repository.delete(find(id)); }

    private Patient find(Long id) {
        return repository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found with id " + id));
    }

    private static void apply(Patient patient, PatientRequest request) {
        patient.setUserId(request.userId());
        patient.setName(request.name().trim());
        patient.setDob(request.dob());
        patient.setGender(request.gender());
        patient.setPhone(request.phone());
        patient.setAddress(request.address());
        patient.setBloodGroup(request.bloodGroup());
    }
}
