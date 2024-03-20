package com.alexbiehl.mycloudnotes;

import java.net.URI;

import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.lang.NonNull;

public class TestUtils {

    @NonNull
    public static URI uri(@NonNull TestRestTemplate restTemplate, @NonNull String path) {
        return restTemplate.getRestTemplate().getUriTemplateHandler().expand(path);
    }
}
