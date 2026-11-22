package com.medical.discovery;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class EurekaServerApplicationTest {
    @LocalServerPort int port;
    @Autowired TestRestTemplate http;

    @Test
    void servesHealthEndpoint() {
        assertEquals(HttpStatus.OK, http.getForEntity("http://localhost:" + port + "/actuator/health", String.class).getStatusCode());
    }
}
