package com.medical.appointment.client;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

@Component
public class DoctorClient {
    private final KafkaLookupClient lookups;
    public DoctorClient(KafkaLookupClient lookups) { this.lookups = lookups; }
    public DoctorRef get(Long id) {
        JsonNode result = lookups.lookup("doctor.lookup.requests", id);
        return result.path("found").asBoolean() ? new DoctorRef(id, result.path("availableDays").asText()) : null;
    }
    public record DoctorRef(Long id, String availableDays) {}
}
