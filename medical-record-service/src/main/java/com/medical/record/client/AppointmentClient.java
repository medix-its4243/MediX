package com.medical.record.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "appointment-service")
public interface AppointmentClient {
    @GetMapping("/appointments/{id}")
    AppointmentRef get(@PathVariable("id") Long id);
    record AppointmentRef(Long id, Long patientId, Long doctorId, String status) {}
}
