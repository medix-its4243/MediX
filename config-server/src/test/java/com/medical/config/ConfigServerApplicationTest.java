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
            "auth-service,auth",
            "patient-service,patient",
            "doctor-service,doctor",
            "appointment-service,appointment",
            "medical-record-service,record",
            "billing-service,billing"
    })
    void servesDatabaseConfiguration(String service, String schema) throws Exception {
        String body = http.getForObject("http://localhost:" + port + "/" + service + "/default", String.class);
        JsonNode response = json.readTree(body);
        Map<String, String> properties = new java.util.HashMap<>();
        for (JsonNode source : response.path("propertySources")) {
            source.path("source").properties().forEach(entry -> properties.putIfAbsent(entry.getKey(), entry.getValue().asText()));
        }
        assertTrue(properties.get("spring.datasource.url").endsWith("/medical_db?currentSchema=" + schema));
        assertEquals(schema, properties.get("spring.jpa.properties.hibernate.default_schema"));
        assertTrue(properties.containsKey("spring.datasource.username"));
        assertTrue(properties.containsKey("spring.datasource.password"));
        assertEquals("validate", properties.get("spring.jpa.hibernate.ddl-auto"));
    }
}
