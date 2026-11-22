package com.medical.record.client;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

@Component
public class AppointmentClient {
    private final KafkaLookupClient lookups;
    public AppointmentClient(KafkaLookupClient lookups) { this.lookups = lookups; }
    public AppointmentRef get(Long id) {
        JsonNode result = lookups.lookup("appointment.lookup.requests", id);
        return result.path("found").asBoolean() ? new AppointmentRef(id, result.path("patientId").asLong(),
                result.path("doctorId").asLong(), result.path("status").asText()) : null;
    }
    public record AppointmentRef(Long id, Long patientId, Long doctorId, String status) {}
}
