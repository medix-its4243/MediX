package com.medical.patient.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.medical.patient.repository.PatientRepository;
import java.util.concurrent.TimeUnit;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class PatientLookupListener {
    private final PatientRepository patients;
    private final KafkaTemplate<String, String> kafka;
    private final ObjectMapper json;

    public PatientLookupListener(PatientRepository patients, KafkaTemplate<String, String> kafka, ObjectMapper json) {
        this.patients = patients;
        this.kafka = kafka;
        this.json = json;
    }

    @KafkaListener(topics = "patient.lookup.requests", groupId = "patient-lookup")
    public void lookup(String message) throws Exception {
        JsonNode request = json.readTree(message);
        long id = request.path("id").asLong();
        ObjectNode reply = json.createObjectNode();
        reply.put("correlationId", request.path("correlationId").asText());
        patients.findById(id).ifPresentOrElse(patient -> {
            reply.put("found", true);
            reply.put("userId", patient.getUserId());
        }, () -> reply.put("found", false));
        kafka.send(request.path("replyTopic").asText(), Long.toString(id), json.writeValueAsString(reply))
                .get(5, TimeUnit.SECONDS);
    }
}
