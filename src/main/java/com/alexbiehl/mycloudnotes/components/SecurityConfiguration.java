package com.alexbiehl.mycloudnotes.components;

import com.alexbiehl.mycloudnotes.api.API;
import com.alexbiehl.mycloudnotes.security.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfiguration {

    @Value("${cors.allowedMethods}")
    private String[] allowedMethods;

    @Value("${cors.allowedHeaders}")
    private String[] allowedHeaders;

    @Value("${cors.allowedOriginPatterns}")
    private String[] allowedOriginPatterns;

    @Value("${cors.exposedHeaders}")
    private String[] exposedHeaders;

    @Value("${frontend.host}")
    private String frontendHost;


    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedHeaders(Arrays.asList(allowedHeaders));
        config.setAllowedMethods(Arrays.asList(allowedMethods));
        config.setAllowedOriginPatterns(Arrays.asList(allowedOriginPatterns));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
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
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorizationManagerRequestMatcherRegistry ->
                        authorizationManagerRequestMatcherRegistry
                                .requestMatchers(
                                        String.format("%s%s", API.USERS, API.REGISTER_USER),
                                        String.format("%s%s", API.AUTH, API.LOGIN_USER),
                                        String.format("%s%s", API.AUTH, API.REFRESH_TOKEN)
                                ).permitAll()
                                //.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                                .requestMatchers("/health-check").permitAll()
                                .requestMatchers("/admin**").hasAuthority("ROLE_ADMIN")
                                .anyRequest().authenticated())
                // specify basic auth
                // .httpBasic(Customizer.withDefaults())
                // enables external login? may not need if we do JWT
                /*.formLogin((formLogin) -> formLogin.loginPage(frontendHost + "/" + API.LOGIN_USER)
                        .defaultSuccessUrl(frontendHost)
                        .permitAll())*/
                /*.formLogin((new Customizer<FormLoginConfigurer<HttpSecurity>>() {
                    @Override
                    public void customize(FormLoginConfigurer<HttpSecurity> httpSecurityFormLoginConfigurer) {
                        httpSecurityFormLoginConfigurer.loginPage(frontendHost + "/" + API.LOGIN_USER);
                    }
                }))*/
                // enables external logout? may not need if we do JWT
                /*.logout((logout) -> logout.logoutUrl(API.LOGOUT_USER)
                        .logoutSuccessUrl(frontendHost + "/" + API.LOGOUT_USER + "?logoutSuccess=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll())*/
                /*
                .logout((new Customizer<LogoutConfigurer<HttpSecurity>>() {
                    @Override
                    public void customize(LogoutConfigurer<HttpSecurity> httpSecurityLogoutConfigurer) {
                        httpSecurityLogoutConfigurer.logoutUrl(frontendHost + "/" + API.LOGOUT_USER);
                    }
                }))*/
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtFilter(), UsernamePasswordAuthenticationFilter.class);
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

    @Bean
    public JwtAuthorizationFilter jwtFilter() {
        return new JwtAuthorizationFilter();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
