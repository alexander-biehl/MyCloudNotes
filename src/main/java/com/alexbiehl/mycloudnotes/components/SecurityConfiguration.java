package com.alexbiehl.mycloudnotes.components;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SecurityConfiguration {

    @Value("${cors.allowedMethods}")
    private String[] allowedMethods;

    @Value("${cors.allowedHeaders}")
    private String[] allowedHeaders;

    @Value("${cors.allowedOriginPatterns}")
    private String[] allowedOriginPatterns;

    @Value("${cors.exposedHeaders}")
    private String[] exposedHeaders;

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @SuppressWarnings("null")
            @Override
            public void addCorsMappings(@NonNull final CorsRegistry registry) {
                registry.addMapping("/notes")
                        .allowedHeaders(allowedHeaders)
                        .allowedMethods(allowedMethods)
                        .allowedOriginPatterns(allowedOriginPatterns)
                        .exposedHeaders(exposedHeaders);
                registry.addMapping("/notes/**")
                        .allowedHeaders(allowedHeaders)
                        .allowedMethods(allowedMethods)
                        .allowedOriginPatterns(allowedOriginPatterns)
                        .exposedHeaders(exposedHeaders);
                registry.addMapping("/health-check")
                        .allowedHeaders(allowedHeaders)
                        .allowedMethods(allowedMethods)
                        .allowedOriginPatterns(allowedOriginPatterns)
                        .exposedHeaders(exposedHeaders);
            }
        };
    }
}
