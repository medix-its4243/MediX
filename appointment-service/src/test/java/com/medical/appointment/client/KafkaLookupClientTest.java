package com.medical.appointment.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class KafkaLookupClientTest {
    @Mock KafkaTemplate<String, String> kafka;
    private final ObjectMapper json = new ObjectMapper();

    @Test
    void matchesReplyByCorrelationId() throws Exception {
        KafkaLookupClient client = new KafkaLookupClient(kafka, json);
        when(kafka.send(anyString(), anyString(), anyString())).thenAnswer(call -> {
            String request = call.getArgument(2);
            String correlationId = json.readTree(request).path("correlationId").asText();
            client.receive("{\"correlationId\":\"" + correlationId + "\",\"found\":true,\"userId\":9}");
            return CompletableFuture.completedFuture(null);
        });
        assertEquals(9L, client.lookup("patient.lookup.requests", 1L).path("userId").asLong());
    }

    @Test
    void returnsServiceUnavailableWhenPublishFails() {
        KafkaLookupClient client = new KafkaLookupClient(kafka, json);
        when(kafka.send(anyString(), anyString(), anyString())).thenReturn(CompletableFuture.failedFuture(new RuntimeException("broker down")));
        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> client.lookup("patient.lookup.requests", 1L));
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, error.getStatusCode());
    }
}
