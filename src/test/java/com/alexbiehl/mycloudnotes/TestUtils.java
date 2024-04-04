package com.alexbiehl.mycloudnotes;

import java.net.URI;
import java.util.UUID;

import com.alexbiehl.mycloudnotes.model.User;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.lang.NonNull;

public class TestUtils {

    @NonNull
    public static URI uri(@NonNull TestRestTemplate restTemplate, @NonNull String path) {
        return restTemplate.getRestTemplate().getUriTemplateHandler().expand(path);
    }

    public static User generateUser() {
        return new User(UUID.randomUUID(), "test", "test", true);
    }
}
