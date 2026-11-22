package com.medical.doctor.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.medical.doctor.repository.DoctorRepository;
import java.util.concurrent.TimeUnit;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class DoctorLookupListener {
    private final DoctorRepository doctors;
    private final KafkaTemplate<String, String> kafka;
    private final ObjectMapper json;

    public DoctorLookupListener(DoctorRepository doctors, KafkaTemplate<String, String> kafka, ObjectMapper json) {
        this.doctors = doctors;
        this.kafka = kafka;
        this.json = json;
    }

    @KafkaListener(topics = "doctor.lookup.requests", groupId = "doctor-lookup")
    public void lookup(String message) throws Exception {
        JsonNode request = json.readTree(message);
        long id = request.path("id").asLong();
        ObjectNode reply = json.createObjectNode();
        reply.put("correlationId", request.path("correlationId").asText());
        doctors.findById(id).ifPresentOrElse(doctor -> {
            reply.put("found", true);
            reply.put("availableDays", doctor.getAvailableDays());
        }, () -> reply.put("found", false));
        kafka.send(request.path("replyTopic").asText(), Long.toString(id), json.writeValueAsString(reply))
                .get(5, TimeUnit.SECONDS);
    }
}
