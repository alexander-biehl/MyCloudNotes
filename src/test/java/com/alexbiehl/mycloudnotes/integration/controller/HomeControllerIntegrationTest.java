package com.alexbiehl.mycloudnotes.integration.controller;

import com.alexbiehl.mycloudnotes.api.API;
import com.alexbiehl.mycloudnotes.utils.TestPostgresContainer;
import com.alexbiehl.mycloudnotes.utils.TestUtils;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;

import com.alexbiehl.mycloudnotes.MycloudnotesApplication;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;

@EnableAutoConfiguration
@SpringBootTest(classes = { MycloudnotesApplication.class }, webEnvironment = WebEnvironment.RANDOM_PORT)
@Testcontainers
public class HomeControllerIntegrationTest {

        @Container
        public static PostgreSQLContainer<TestPostgresContainer> postgreSQLContainer = TestPostgresContainer.getInstance();

        @LocalServerPort
        private int port;

        @Autowired
        private TestRestTemplate restTemplate;

        @Test
        public void testHealthCheck_withoutCors() {
                ResponseEntity<String> response = this.restTemplate.exchange(
                                RequestEntity.get(
                                                TestUtils.uri(this.restTemplate, API.HEALTH_CHECK)).build(),
                                String.class);

                assertEquals(response.getStatusCode(), HttpStatus.OK);
                assertEquals(response.getBody(), "OK");
        }

        @Test
        public void testHealthCheck_withCors() {
                ResponseEntity<String> response = this.restTemplate.exchange(
                                RequestEntity.get(TestUtils.uri(this.restTemplate, API.HEALTH_CHECK))
                                                .header(HttpHeaders.ORIGIN, "http://localhost:9000")
                                                .build(),
                                String.class);
                assertEquals(response.getStatusCode(), HttpStatus.OK);
                assertEquals(response.getBody(), "OK");
                assertEquals(response.getHeaders().getAccessControlAllowOrigin(), "http://localhost:9000");
        }

}
