package com.medical.doctor.controller;

import com.medical.doctor.dto.DoctorRequest;
import com.medical.doctor.dto.DoctorResponse;
import com.medical.doctor.entity.Doctor;
import com.medical.doctor.repository.DoctorRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/doctors")
public class DoctorController {
    private final DoctorRepository doctors;
    public DoctorController(DoctorRepository doctors) { this.doctors = doctors; }

    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public DoctorResponse create(@Valid @RequestBody DoctorRequest request) {
        Doctor doctor = new Doctor();
        doctor.setUserId(request.userId());
        doctor.setName(request.name().trim());
        doctor.setSpecialization(request.specialization());
        doctor.setPhone(request.phone());
        doctor.setConsultationFee(request.consultationFee());
        doctor.setAvailableDays(request.availableDays());
        return DoctorResponse.from(doctors.save(doctor));
    }

    @GetMapping("/{id}")
    public DoctorResponse get(@PathVariable Long id) {
        return DoctorResponse.from(doctors.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor not found with id " + id)));
    }
}
