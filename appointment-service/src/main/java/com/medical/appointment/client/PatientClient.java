package com.medical.appointment.client;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

@Component
public class PatientClient {
    private final KafkaLookupClient lookups;
    public PatientClient(KafkaLookupClient lookups) { this.lookups = lookups; }
    public PatientRef get(Long id) {
        JsonNode result = lookups.lookup("patient.lookup.requests", id);
        return result.path("found").asBoolean() ? new PatientRef(id, result.path("userId").asLong()) : null;
    }
    public record PatientRef(Long id, Long userId) {}
}
