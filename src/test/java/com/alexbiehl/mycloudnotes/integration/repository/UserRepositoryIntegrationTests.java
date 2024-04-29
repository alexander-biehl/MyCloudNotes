package com.alexbiehl.mycloudnotes.integration.repository;

import com.alexbiehl.mycloudnotes.repository.UserRepository;
import com.alexbiehl.mycloudnotes.utils.TestConstants;
import com.alexbiehl.mycloudnotes.utils.TestPostgresContainer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
public class UserRepositoryIntegrationTests {

    @Container
    public static PostgreSQLContainer<TestPostgresContainer> postgreSQLContainer = TestPostgresContainer.getInstance();

    @Autowired
    private UserRepository userRepository;

    @Test
    public void testUserExists_AndOk() {
        assertTrue(userRepository.existsByUsername("test_user"));
    }

    @Test
    public void testUserExists_AndNo() {
        assertFalse(userRepository.existsByUsername("does_not_exist"));
    }

    @Test
    public void testAdminExists_andOk() { assertTrue(userRepository.existsById(TestConstants.TEST_ADMIN_ID));}
}
