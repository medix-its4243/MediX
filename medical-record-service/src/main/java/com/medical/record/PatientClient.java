package com.medical.record;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "patient-service")
public interface PatientClient {
    @GetMapping("/patients/{id}")
    PatientRef get(@PathVariable("id") Long id);
    record PatientRef(Long id, Long userId) {}
}
