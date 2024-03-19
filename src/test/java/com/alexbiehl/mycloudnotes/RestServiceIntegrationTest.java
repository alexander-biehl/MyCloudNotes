package com.alexbiehl.mycloudnotes;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

import com.alexbiehl.mycloudnotes.dto.Note;

@SpringBootTest(classes = MycloudnotesApplication.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@RunWith(SpringRunner.class)
public class RestServiceIntegrationTest {

    @SuppressWarnings("unused")
    private static final Logger logger = LoggerFactory.getLogger(RestServiceIntegrationTest.class);

    @Container
    private static PostgreSQLContainer<TestPostgresContainer> container = TestPostgresContainer.getInstance();

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void corsWithJavaconfig() {
        ResponseEntity<Note[]> entity = this.restTemplate.exchange(
                RequestEntity.get(uri("/notes"))
                        .header(HttpHeaders.ORIGIN, "http://localhost:5173")
                        .header(HttpHeaders.CONTENT_TYPE, "application/json")
                        .build(),
                Note[].class);
        assertEquals(HttpStatus.OK, entity.getStatusCode());
        assertEquals("http://localhost:5173",
                entity.getHeaders().getAccessControlAllowOrigin());
    }

    @NonNull
    private URI uri(@NonNull String path) {
        return restTemplate.getRestTemplate().getUriTemplateHandler().expand(path);
    }

}
