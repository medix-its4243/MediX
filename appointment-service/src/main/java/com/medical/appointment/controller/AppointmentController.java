package com.medical.appointment.controller;

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/appointments")
public class AppointmentController {
    private final AppointmentService service;
    public AppointmentController(AppointmentService service) { this.service = service; }

    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public AppointmentResponse book(@Valid @RequestBody BookingRequest request) { return service.book(request); }
    @GetMapping("/{id}")
    public AppointmentResponse get(@PathVariable Long id) { return service.get(id); }
    @GetMapping("/patient/{id}")
    public List<AppointmentResponse> forPatient(@PathVariable Long id) { return service.forPatient(id); }
    @PutMapping("/{id}/cancel")
    public AppointmentResponse cancel(@PathVariable Long id) { return service.cancel(id); }
    @PutMapping("/{id}/complete")
    public AppointmentResponse complete(@PathVariable Long id) { return service.complete(id); }
}
