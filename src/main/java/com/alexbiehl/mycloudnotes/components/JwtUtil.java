package com.alexbiehl.mycloudnotes.components;

import com.alexbiehl.mycloudnotes.model.Role;
import com.alexbiehl.mycloudnotes.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class JwtUtil {

    public static final String ROLE_CLAIM_ID = "ROLES";
    public static final String TOKEN_HEADER = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String ISSUER = "MyCloudNotes";

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${jwt.secret.key}")
    private String secretKey;
    @Value("${jwt.token.validity}")
    private long accessTokenValidity;

    public JwtUtil() {
    }

    /**
     * Provisions and signs a JWT Token for the specified User.
     *
     * @param user - The authenticated User
     * @return Signed JWT as a string.
     */
    public String createToken(User user) {
        LOGGER.info("Creating JWT token for user: {}", user.toString());
        Date tokenCreated = new Date();
        Date tokenValidity = new Date(tokenCreated.getTime() + TimeUnit.MINUTES.toMillis(accessTokenValidity));
        return Jwts
                .builder()
                .subject(user.getUsername())
                .issuer(ISSUER)
                .expiration(tokenValidity)
                .issuedAt(tokenCreated)
                .claim(ROLE_CLAIM_ID, user
                        .getRoles()
                        .stream()
                        .map(r -> r.getId() + ":" + r.getName())
                        .collect(Collectors.joining(",")))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Validates that the String token is valid and correctly signed.
     *
     * @param token - string representing the raw JWT Token
     * @return validated JWT Claims
     */
    private Claims parseJwtClaims(final String token) {
        LOGGER.info("Parse JWT claims from token: {}", token);
        return Jwts
                .parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Extracts the Token's Claims from the HttpServletRequest object.
     *
     * @param request - HttpServletRequest object
     * @return the JWT Claims or null
     */
    public Claims resolveClaims(HttpServletRequest request) {
        String token = resolveToken(request);
        if (token != null) {
            return parseJwtClaims(token);
        }
        return null;
    }

    /**
     * Extracts the raw JWT string from the {@code HttpServletRequest} request object.
     *
     * @param request the HttpServletRequest object
     * @return the raw jwt token string or null
     */
    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(TOKEN_HEADER);
        if (bearerToken != null && bearerToken.startsWith(TOKEN_PREFIX)) {
            return bearerToken.substring(TOKEN_PREFIX.length()).trim();
        }
        return null;
    }

    /**
     * Validates that the JWT Claims are still valid. I.E. that the experiation
     * is after the current date/time.
     *
     * @param claims JWT Claims object
     * @return true if valid, false otherwise
     */
    public boolean validateClaims(Claims claims) {
        return claims.getExpiration().after(new Date());
    }

    /**
     * Creates a signing key from the base64 encoded secret.
     *
     * @return a Key object for signing the JWT
     */
    private SecretKey getSigningKey() {
        // Decode the base64 encoded secret key
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Extracts the username from the validated JWT Claims.
     *
     * @param claims - The validated JWT Claims
     * @return the username as a string
     */
    public String getUsername(Claims claims) {
        return claims.getSubject();
    }

    /**
     * Parses the list of Roles from the validated JWT Claims object.
     *
     * @param claims - the validated JWT Claims
     * @return List of Role objects.
     */
    public List<Role> getRoles(Claims claims) {
        return Arrays.stream(claims
                        .get(ROLE_CLAIM_ID)
                        .toString()
                        .split(","))
                .map(s -> {
                    String[] split = s.split(":");
                    return new Role(UUID.fromString(split[0]), split[1]);
                })
                .toList();
    }
}
