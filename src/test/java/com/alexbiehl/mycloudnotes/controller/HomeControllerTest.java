package com.alexbiehl.mycloudnotes.controller;

import static org.junit.Assert.assertEquals;

import java.net.URI;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
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
import org.springframework.lang.NonNull;
import org.springframework.test.context.junit4.SpringRunner;

import com.alexbiehl.mycloudnotes.MycloudnotesApplication;

@RunWith(SpringRunner.class)
@EnableAutoConfiguration
@SpringBootTest(classes = { MycloudnotesApplication.class }, webEnvironment = WebEnvironment.RANDOM_PORT)
public class HomeControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void testHealthCheck_withoutCors() {
        ResponseEntity<String> response = this.restTemplate.exchange(
                RequestEntity.get(uri("/health-check")).build(), String.class);

        assertEquals(response.getStatusCode(), HttpStatus.OK);
        assertEquals(response.getBody(), "OK");
    }

    @Test
    public void testHealthCheck_withCors() {
        ResponseEntity<String> response = this.restTemplate.exchange(
                RequestEntity.get(uri("/health-check"))
                        .header(HttpHeaders.ORIGIN, "http://localhost:9000")
                        .build(),
                String.class);
        assertEquals(response.getStatusCode(), HttpStatus.OK);
        assertEquals(response.getBody(), "OK");
        assertEquals(response.getHeaders().getAccessControlAllowOrigin(), "http://localhost:9000");
    }

    @NonNull
    private URI uri(@NonNull String path) {
        return restTemplate.getRestTemplate().getUriTemplateHandler().expand(path);
    }
}
