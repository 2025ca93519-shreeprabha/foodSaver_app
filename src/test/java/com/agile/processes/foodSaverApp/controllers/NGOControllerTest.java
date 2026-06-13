package com.agile.processes.foodSaverApp.controllers;

import com.agile.processes.foodSaverApp.entities.User;
import com.agile.processes.foodSaverApp.enums.Role;
import com.agile.processes.foodSaverApp.repository.NGORepository;
import com.agile.processes.foodSaverApp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;NON_KEYWORDS=USER",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.properties.hibernate.globally_quoted_identifiers=true"
})
public class NGOControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NGORepository ngoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User ngoUser;
    private User restaurantUser;

    @BeforeEach
    void setUp() {
        ngoRepository.deleteAll();
        userRepository.deleteAll();

        // Setup a test NGO user
        User user1 = new User();
        user1.setName("Test NGO Owner");
        user1.setEmail("ngo@example.com");
        user1.setPassword(passwordEncoder.encode("password123"));
        user1.setRole(Role.NGO);
        ngoUser = userRepository.save(user1);

        // Setup a test restaurant user
        User user2 = new User();
        user2.setName("Test Restaurant Owner");
        user2.setEmail("restaurant@example.com");
        user2.setPassword(passwordEncoder.encode("password123"));
        user2.setRole(Role.RESTAURANT);
        restaurantUser = userRepository.save(user2);
    }

    @Test
    void testRegisterNGO_Success() throws Exception {
        String requestJson = "{"
                + "\"userId\":" + ngoUser.getId() + ","
                + "\"name\":\"Feed The Hungry\","
                + "\"address\":\"789 Charity Road\","
                + "\"phone\":\"555-0155\""
                + "}";

        mockMvc.perform(post("/register/ngo")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(content().string(containsString("NGO registered successfully")));

        assertTrue(ngoRepository.existsByUser(ngoUser));
    }

    @Test
    void testRegisterNGO_InvalidUserRole() throws Exception {
        String requestJson = "{"
                + "\"userId\":" + restaurantUser.getId() + ","
                + "\"name\":\"Feed The Hungry\","
                + "\"address\":\"789 Charity Road\","
                + "\"phone\":\"555-0155\""
                + "}";

        mockMvc.perform(post("/register/ngo")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("User does not have NGO role")));
    }

    @Test
    void testRegisterNGO_UserNotFound() throws Exception {
        String requestJson = "{"
                + "\"userId\":99999,"
                + "\"name\":\"Feed The Hungry\","
                + "\"address\":\"789 Charity Road\","
                + "\"phone\":\"555-0155\""
                + "}";

        mockMvc.perform(post("/register/ngo")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("User not found")));
    }

    @Test
    void testRegisterNGO_DuplicateRegistration() throws Exception {
        String requestJson = "{"
                + "\"userId\":" + ngoUser.getId() + ","
                + "\"name\":\"Feed The Hungry\","
                + "\"address\":\"789 Charity Road\","
                + "\"phone\":\"555-0155\""
                + "}";

        // First registration
        mockMvc.perform(post("/register/ngo")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isCreated());

        // Second registration
        mockMvc.perform(post("/register/ngo")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("NGO is already registered for this user")));
    }

    @Test
    void testRegisterNGO_ValidationErrors() throws Exception {
        String requestJson = "{"
                + "\"userId\":null,"
                + "\"name\":\"\","
                + "\"address\":\"\","
                + "\"phone\":\"\""
                + "}";

        mockMvc.perform(post("/register/ngo")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("User ID is required")))
                .andExpect(content().string(containsString("NGO name is required")))
                .andExpect(content().string(containsString("Address is required")))
                .andExpect(content().string(containsString("Phone number is required")));
    }
}
