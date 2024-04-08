package com.alexbiehl.mycloudnotes.components;

import com.alexbiehl.mycloudnotes.security.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebSecurity
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
                registry.addMapping("/user**")
                        .allowedHeaders(allowedHeaders)
                        .allowedMethods(allowedMethods)
                        .allowedOriginPatterns(allowedOriginPatterns)
                        .exposedHeaders(exposedHeaders);
            }
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http

                // exclude CORS pre-flight checks from auth
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(authorizationManagerRequestMatcherRegistry ->
                        authorizationManagerRequestMatcherRegistry
                                .requestMatchers("/user**").permitAll()
                                .requestMatchers("/health-check").permitAll()
                                .requestMatchers("/admin**").hasAuthority("ROLE_ADMIN")
                                .anyRequest().authenticated())
                // specify basic auth
                .httpBasic(Customizer.withDefaults())
                .authenticationProvider(authenticationProvider());
        return http.build();
    }

    @Bean
    public UserDetailsServiceImpl userDetailsService() {
        return new UserDetailsServiceImpl();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
}
