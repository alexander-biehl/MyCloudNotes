package com.alexbiehl.mycloudnotes.components;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtAuthorizationFilter.class);

    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private ObjectMapper objectMapper;

//    public JwtAuthorizationFilter(JwtUtil jwtUtil, ObjectMapper objectMapper) {
//        this.jwtUtil = jwtUtil;
//        this.objectMapper = objectMapper;
//    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        Map<String, Object> errorDetails = new HashMap<>();

        try {
            String accessToken = jwtUtil.resolveToken(request);
            if (accessToken == null) {
                filterChain.doFilter(request, response);
                return;
            }
            LOGGER.info("Token: " + accessToken);
            Claims claims = jwtUtil.resolveClaims(request);

            if (claims != null && jwtUtil.validateClaims(claims)) {
                // retrieve the username
                String username = claims.getSubject();
                LOGGER.info("Username: " + username);
                // retrive the raw claims role string
                String roleClaims = claims.get(JwtUtil.ROLE_CLAIM_ID).toString();
                LOGGER.info("Role string: " + roleClaims);
                // convert the claims string into list of Authorities
                List<SimpleGrantedAuthority> resolvedRoles = Arrays.stream(roleClaims.split(","))
                        .map(claimStr -> new SimpleGrantedAuthority(claimStr.split(":")[1])).toList();
                Authentication authentication =
                        new UsernamePasswordAuthenticationToken(username, "", resolvedRoles);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ex) {
            errorDetails.put("message", "Authentication error");
            errorDetails.put("details", ex.getMessage());
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            objectMapper.writeValue(response.getWriter(), errorDetails);
        }
        filterChain.doFilter(request, response);
    }
}
