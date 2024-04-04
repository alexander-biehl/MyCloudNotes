//package com.alexbiehl.mycloudnotes;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//
//import com.alexbiehl.mycloudnotes.components.SecurityConfiguration;
//import org.junit.jupiter.api.Test;
//import org.junit.runner.RunWith;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.web.client.TestRestTemplate;
//import org.springframework.context.annotation.Import;
//import org.springframework.http.*;
//import org.springframework.security.test.context.support.WithUserDetails;
//import org.springframework.test.context.junit4.SpringRunner;
//import org.springframework.test.web.servlet.MockMvc;
//import org.testcontainers.containers.PostgreSQLContainer;
//import org.testcontainers.junit.jupiter.Container;
//
//import com.alexbiehl.mycloudnotes.model.Note;
//
//import java.util.UUID;
//
//@Import(SecurityConfiguration.class)
//@SpringBootTest(classes = MycloudnotesApplication.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
//@RunWith(SpringRunner.class)
//@AutoConfigureMockMvc
//public class RestServiceIntegrationTest {
//
//        @SuppressWarnings("unused")
//        private static final Logger logger = LoggerFactory.getLogger(RestServiceIntegrationTest.class);
//
//        @Container
//        private static PostgreSQLContainer<TestPostgresContainer> container = TestPostgresContainer.getInstance();
//
//        @Autowired
//        private TestRestTemplate restTemplate;
//
//        @Autowired
//        private MockMvc mockMvc;
//
//        @Test
//        @WithUserDetails()
//        public void corsWithJavaconfig() {
//                ResponseEntity<Note[]> entity = this.restTemplate.exchange(
//                                RequestEntity.get(TestUtils.uri(this.restTemplate, "/notes"))
//                                                .header(HttpHeaders.ORIGIN, "http://localhost:5173")
//                                                .header(HttpHeaders.CONTENT_TYPE, "application/json")
//                                                .build(),
//                                Note[].class);
//                assertEquals(HttpStatus.OK, entity.getStatusCode());
//                assertEquals("http://localhost:5173",
//                                entity.getHeaders().getAccessControlAllowOrigin());
//        }
//
//}
