package com.medical.appointment.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.medical.appointment.repository.AppointmentRepository;
import java.util.concurrent.TimeUnit;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class AppointmentLookupListener {
    private final AppointmentRepository appointments;
    private final KafkaTemplate<String, String> kafka;
    private final ObjectMapper json;

    public AppointmentLookupListener(AppointmentRepository appointments, KafkaTemplate<String, String> kafka, ObjectMapper json) {
        this.appointments = appointments;
        this.kafka = kafka;
        this.json = json;
    }

    @KafkaListener(topics = "appointment.lookup.requests", groupId = "appointment-lookup")
    public void lookup(String message) throws Exception {
        JsonNode request = json.readTree(message);
        long id = request.path("id").asLong();
        ObjectNode reply = json.createObjectNode();
        reply.put("correlationId", request.path("correlationId").asText());
        appointments.findById(id).ifPresentOrElse(appointment -> {
            reply.put("found", true);
            reply.put("patientId", appointment.getPatientId());
            reply.put("doctorId", appointment.getDoctorId());
            reply.put("status", appointment.getStatus().name());
        }, () -> reply.put("found", false));
        kafka.send(request.path("replyTopic").asText(), Long.toString(id), json.writeValueAsString(reply))
                .get(5, TimeUnit.SECONDS);
    }
}
