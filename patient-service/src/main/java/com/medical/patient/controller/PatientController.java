package com.medical.patient.controller;

import com.medical.patient.dto.PatientRequest;
import com.medical.patient.dto.PatientResponse;
import com.medical.patient.service.PatientService;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/patients")
public class PatientController {
    private final PatientService service;
    public PatientController(PatientService service) { this.service = service; }

    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public PatientResponse create(@Valid @RequestBody PatientRequest request,
                                  @RequestHeader(value = "X-User-Role", required = false) String role,
                                  @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        checkAccess(role, userId, request.userId());
        return service.create(request);
    }
    @GetMapping
    public List<PatientResponse> list(@RequestHeader(value = "X-User-Role", required = false) String role,
                                      @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if ("PATIENT".equals(role) && userId != null) return service.forUser(userId);
        checkAccess(role, userId, null);
        return service.list();
    }
    @GetMapping("/{id}")
    public PatientResponse get(@PathVariable Long id,
                               @RequestHeader(value = "X-User-Role", required = false) String role,
                               @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        PatientResponse patient = service.get(id);
        checkAccess(role, userId, patient.userId());
        return patient;
    }
    @PutMapping("/{id}")
    public PatientResponse update(@PathVariable Long id, @Valid @RequestBody PatientRequest request,
                                  @RequestHeader(value = "X-User-Role", required = false) String role,
                                  @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        checkAccess(role, userId, service.get(id).userId());
        checkAccess(role, userId, request.userId());
        return service.update(id, request);
    }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id,
                       @RequestHeader(value = "X-User-Role", required = false) String role,
                       @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        checkAccess(role, userId, service.get(id).userId());
        service.delete(id);
    }

    private static void checkAccess(String role, Long userId, Long ownerId) {
        if ("ADMIN".equals(role) || "STAFF".equals(role)) return;
        if ("PATIENT".equals(role) && userId != null && userId.equals(ownerId)) return;
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access to this patient is forbidden");
    }
}
