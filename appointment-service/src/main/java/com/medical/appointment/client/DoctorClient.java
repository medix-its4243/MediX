package com.medical.appointment.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "doctor-service")
public interface DoctorClient {
    @GetMapping("/doctors/{id}")
    DoctorRef get(@PathVariable("id") Long id);
    record DoctorRef(Long id, String availableDays) {}
}
