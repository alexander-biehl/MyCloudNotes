package com.alexbiehl.mycloudnotes.components;

import com.alexbiehl.mycloudnotes.model.Role;
import com.alexbiehl.mycloudnotes.model.User;
import io.jsonwebtoken.lang.Collections;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class JwtUtilTests {

    private static final String testKey = "ba4c3cd26cc69e7bd21a739c131b6de4029ab82d25e0f5af98532a00db68b51e";
    private static final long tokenValidity = 60;
    private static JwtUtil jwtUtil;
    @Mock
    private User testUser;
    @Mock
    private Role testRole;

    @BeforeAll
    public static void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secretKey", testKey);
        ReflectionTestUtils.setField(jwtUtil, "accessTokenValidity", tokenValidity);
    }

    @Test
    public void createJwtUtilTest() {
        assertNotNull(jwtUtil);
    }

    @Test
    public void givenUserWithRole_testCreateToken() {

        when(testUser.getUsername()).thenReturn("test_user");
        when(testUser.getRoles()).thenReturn(Collections.setOf(testRole));
        when(testRole.getId()).thenReturn(UUID.randomUUID());
        when(testRole.getName()).thenReturn("USER");

        String token = jwtUtil.createToken(testUser);
        assertNotNull(token);
    }
}
