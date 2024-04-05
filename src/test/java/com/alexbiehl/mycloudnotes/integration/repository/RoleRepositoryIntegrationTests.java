package com.alexbiehl.mycloudnotes.integration.repository;

import com.alexbiehl.mycloudnotes.model.Role;
import com.alexbiehl.mycloudnotes.repository.RoleRepository;
import com.alexbiehl.mycloudnotes.utils.TestConstants;
import com.alexbiehl.mycloudnotes.utils.TestPostgresContainer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
public class RoleRepositoryIntegrationTests {

    @Container
    private static PostgreSQLContainer<TestPostgresContainer> container = TestPostgresContainer.getInstance();

    @Autowired
    private RoleRepository roleRepository;

    @Test
    public void ensureUserRole() {
        Role userRole = roleRepository.getReferenceById(TestConstants.USER_ROLE_ID);
        assertNotNull(userRole);
        assertEquals(TestConstants.USER_ROLE_NAME, userRole.getName());
    }

    @Test
    public void ensureAdminRole() {
        Role adminRole = roleRepository.getReferenceById(TestConstants.ADMIN_ROLE_ID);
        assertNotNull(adminRole);
        assertEquals(TestConstants.ADMIN_ROLE_NAME, adminRole.getName());
    }
}
