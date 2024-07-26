package com.alexbiehl.mycloudnotes.components;

import com.alexbiehl.mycloudnotes.model.Role;
import com.alexbiehl.mycloudnotes.security.UserPrincipal;
import com.alexbiehl.mycloudnotes.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtAuthorizationFilter.class);

    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ApplicationContext applicationContext;


    // lazy load UserService bean to prevent circular relationships
    private UserService userService() {
        return applicationContext.getBean(UserService.class);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String accessToken = jwtUtil.resolveToken(request);
        if (accessToken == null) {
            filterChain.doFilter(request, response);
            return;
        }

        Claims claims = null;
        LOGGER.info("Token: " + accessToken);
        try {
            claims = jwtUtil.resolveClaims(request);
        } catch (SignatureException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid Signature");
            return;
        } catch (MalformedJwtException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid JWT Format");
            return;
        } catch (ExpiredJwtException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Expired Token");
            return;
        }

        if (claims != null && jwtUtil.validateClaims(claims)) {
            LOGGER.info("Validated JWT Claims");
            // retrieve the username
            String username = claims.getSubject();
            LOGGER.info("Username: " + username);
            UserPrincipal userPrincipal = new UserPrincipal(userService().getUserByUsername(username));
            // retrive the raw claims role string
            List<Role> roles = jwtUtil.getRoles(claims);
            // convert the list of roles to list of authorities
            List<SimpleGrantedAuthority> resolvedRoles = roles
                    .stream()
                    .map(role -> new SimpleGrantedAuthority(role.getName()))
                    .toList();

            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(userPrincipal, userPrincipal.getPassword(), resolvedRoles);
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authToken);
        } else {
            LOGGER.error("Unable to validate claims");
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid or Expired Token");
        }

        filterChain.doFilter(request, response);
    }
}
