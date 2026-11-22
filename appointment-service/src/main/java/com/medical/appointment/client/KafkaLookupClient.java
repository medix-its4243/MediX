package com.medical.appointment.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class KafkaLookupClient {
    private static final String REPLY_TOPIC = "appointment.lookup.replies";
    private final KafkaTemplate<String, String> kafka;
    private final ObjectMapper json;
    private final ConcurrentHashMap<String, CompletableFuture<JsonNode>> pending = new ConcurrentHashMap<>();

    public KafkaLookupClient(KafkaTemplate<String, String> kafka, ObjectMapper json) {
        this.kafka = kafka;
        this.json = json;
    }

    public JsonNode lookup(String topic, Long id) {
        String correlationId = UUID.randomUUID().toString();
        CompletableFuture<JsonNode> reply = new CompletableFuture<>();
        pending.put(correlationId, reply);
        try {
            ObjectNode request = json.createObjectNode();
            request.put("correlationId", correlationId);
            request.put("replyTopic", REPLY_TOPIC);
            request.put("id", id);
            kafka.send(topic, id.toString(), json.writeValueAsString(request)).get(5, TimeUnit.SECONDS);
            return reply.get(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Kafka lookup interrupted", e);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Kafka lookup failed or timed out", e);
        } finally {
            pending.remove(correlationId);
        }
    }

    @KafkaListener(topics = REPLY_TOPIC, groupId = "appointment-replies-${random.uuid}")
    public void receive(String message) throws Exception {
        JsonNode response = json.readTree(message);
        CompletableFuture<JsonNode> future = pending.get(response.path("correlationId").asText());
        if (future != null) future.complete(response);
    }
}
