package com.medical.record;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/records")
public class MedicalRecordController {
    private final MedicalRecordService service;
    public MedicalRecordController(MedicalRecordService service) { this.service = service; }

    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public RecordResponse create(@Valid @RequestBody RecordRequest request,
                                 @RequestHeader(value = "X-User-Role", required = false) String role) {
        return service.create(request, role);
    }
    @GetMapping("/{id}")
    public RecordResponse get(@PathVariable Long id,
                              @RequestHeader(value = "X-User-Role", required = false) String role,
                              @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return service.get(id, role, userId);
    }
    @GetMapping("/patient/{id}")
    public List<RecordResponse> forPatient(@PathVariable Long id,
                                           @RequestHeader(value = "X-User-Role", required = false) String role,
                                           @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return service.forPatient(id, role, userId);
    }
}
