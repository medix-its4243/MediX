package com.medical.doctor.config;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medical.doctor.entity.Doctor;
import com.medical.doctor.repository.DoctorRepository;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

@ExtendWith(MockitoExtension.class)
class DoctorLookupListenerTest {
    @Mock DoctorRepository doctors;
    @Mock KafkaTemplate<String, String> kafka;
    private final ObjectMapper json = new ObjectMapper();

    @Test
    void repliesWithDoctorAvailabilityAndCorrelationId() throws Exception {
        Doctor doctor = new Doctor();
        doctor.setConsultationFee(new java.math.BigDecimal("2500.00"));
        doctor.setAvailableDays("MON,WED");
        when(doctors.findById(3L)).thenReturn(Optional.of(doctor));
        when(kafka.send(anyString(), anyString(), anyString())).thenAnswer(call -> {
            var reply = json.readTree((String) call.getArgument(2));
            assertTrue(reply.path("found").asBoolean());
            assertTrue(new java.math.BigDecimal("2500.00").compareTo(reply.path("consultationFee").decimalValue()) == 0);
            assertTrue("MON,WED".equals(reply.path("availableDays").asText()));
            assertTrue("abc".equals(reply.path("correlationId").asText()));
            return CompletableFuture.completedFuture(null);
        });
        new DoctorLookupListener(doctors, kafka, json).lookup("{\"correlationId\":\"abc\",\"replyTopic\":\"appointment.lookup.replies\",\"id\":3}");
    }
}
