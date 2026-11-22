package com.medical.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.test.web.client.TestRestTemplate;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ConfigServerApplicationTest {
    @LocalServerPort int port;
    @Autowired TestRestTemplate http;
    @Autowired ObjectMapper json;

    @ParameterizedTest
    @CsvSource({
            "auth-service,auth_db",
            "patient-service,patient_db",
            "doctor-service,doctor_db",
            "appointment-service,appointment_db",
            "medical-record-service,record_db",
            "billing-service,billing_db"
    })
    void servesDatabaseConfiguration(String service, String database) throws Exception {
        String body = http.getForObject("http://localhost:" + port + "/" + service + "/default", String.class);
        JsonNode response = json.readTree(body);
        Map<String, String> properties = new java.util.HashMap<>();
        for (JsonNode source : response.path("propertySources")) {
            source.path("source").properties().forEach(entry -> properties.putIfAbsent(entry.getKey(), entry.getValue().asText()));
        }
        assertTrue(properties.get("spring.datasource.url").endsWith("/" + database));
        assertTrue(properties.containsKey("spring.datasource.username"));
        assertTrue(properties.containsKey("spring.datasource.password"));
        assertEquals("validate", properties.get("spring.jpa.hibernate.ddl-auto"));
    }
}
