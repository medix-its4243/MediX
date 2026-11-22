package com.medical.appointment.controller;

import com.medical.appointment.client.PatientClient;
import com.medical.appointment.dto.AppointmentResponse;
import com.medical.appointment.dto.BookingRequest;
import com.medical.appointment.service.AppointmentService;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/appointments")
public class AppointmentController {
    private final AppointmentService service;
    private final PatientClient patients;
    public AppointmentController(AppointmentService service, PatientClient patients) {
        this.service = service;
        this.patients = patients;
    }

    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public AppointmentResponse book(@Valid @RequestBody BookingRequest request,
                                    @RequestHeader(value = "X-User-Role", required = false) String role,
                                    @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (!"STAFF".equals(role)) checkPatientAccess(role, userId, request.patientId());
        return service.book(request);
    }
    @GetMapping("/{id}")
    public AppointmentResponse get(@PathVariable Long id,
                                   @RequestHeader(value = "X-User-Role", required = false) String role,
                                   @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        AppointmentResponse appointment = service.get(id);
        if (!"ADMIN".equals(role) && !"STAFF".equals(role) && !"DOCTOR".equals(role))
            checkPatientAccess(role, userId, appointment.patientId());
        return appointment;
    }
    @GetMapping("/patient/{id}")
    public List<AppointmentResponse> forPatient(@PathVariable Long id,
                                                @RequestHeader(value = "X-User-Role", required = false) String role,
                                                @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (!"ADMIN".equals(role) && !"STAFF".equals(role) && !"DOCTOR".equals(role))
            checkPatientAccess(role, userId, id);
        return service.forPatient(id);
    }
    @PutMapping("/{id}/cancel")
    public AppointmentResponse cancel(@PathVariable Long id,
                                      @RequestHeader(value = "X-User-Role", required = false) String role,
                                      @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (!"STAFF".equals(role)) checkPatientAccess(role, userId, service.get(id).patientId());
        return service.cancel(id);
    }
    @PutMapping("/{id}/complete")
    public AppointmentResponse complete(@PathVariable Long id,
                                        @RequestHeader(value = "X-User-Role", required = false) String role) {
        if (!"DOCTOR".equals(role)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only doctors can complete appointments");
        return service.complete(id);
    }

    private void checkPatientAccess(String role, Long userId, Long patientId) {
        if (!"PATIENT".equals(role) || userId == null)
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access to this appointment is forbidden");
        PatientClient.PatientRef patient = patients.get(patientId);
        if (patient == null || !userId.equals(patient.userId()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access to this appointment is forbidden");
    }
}
