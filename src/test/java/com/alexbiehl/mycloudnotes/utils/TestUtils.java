package com.alexbiehl.mycloudnotes.utils;

import java.net.URI;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import com.alexbiehl.mycloudnotes.api.API;
import com.alexbiehl.mycloudnotes.components.JwtUtil;
import com.alexbiehl.mycloudnotes.model.User;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestUtils {

    @NonNull
    public static URI uri(@NonNull TestRestTemplate restTemplate, @NonNull String path) {
        return restTemplate.getRestTemplate().getUriTemplateHandler().expand(path);
    }

    public static User generateUser() {
        return new User(UUID.randomUUID(), "test", "test", true);
    }

    public static HttpHeaders headers(String origin) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.setOrigin(origin);
        return headers;
    }

    public static HttpHeaders headers(String origin, String authorization) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.setOrigin(origin);
        headers.set(HttpHeaders.AUTHORIZATION, authorization);
        return headers;
    }

    public static void assertCodeAndOrigin(ResponseEntity<?> entity, HttpStatus status, String origin) {
        assertEquals(status, entity.getStatusCode());
        assertEquals(origin, entity.getHeaders().getAccessControlAllowOrigin());
    }

    public static String getBearerToken(final String token) {
        return String.format("%s %s", JwtUtil.TOKEN_PREFIX, token);
    }
}
