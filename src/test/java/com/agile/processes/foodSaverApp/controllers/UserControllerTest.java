package com.agile.processes.foodSaverApp.controllers;

import com.agile.processes.foodSaverApp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;NON_KEYWORDS=USER",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.properties.hibernate.globally_quoted_identifiers=true"
})
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void testRegisterUser_Success() throws Exception {
        String requestJson = "{"
                + "\"name\":\"John Doe\","
                + "\"email\":\"john@example.com\","
                + "\"password\":\"password123\","
                + "\"role\":\"NGO\""
                + "}";

        mockMvc.perform(post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(content().string(containsString("User registered successfully")));

        assertTrue(userRepository.existsByEmail("john@example.com"));
    }

    @Test
    void testRegisterUser_DuplicateEmail() throws Exception {
        String request1Json = "{"
                + "\"name\":\"John Doe\","
                + "\"email\":\"john@example.com\","
                + "\"password\":\"password123\","
                + "\"role\":\"NGO\""
                + "}";

        mockMvc.perform(post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request1Json))
                .andExpect(status().isCreated());

        String request2Json = "{"
                + "\"name\":\"Jane Doe\","
                + "\"email\":\"john@example.com\","
                + "\"password\":\"password456\","
                + "\"role\":\"RESTAURANT\""
                + "}";

        mockMvc.perform(post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request2Json))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Email is already in use")));
    }

    @Test
    void testRegisterUser_ValidationErrors() throws Exception {
        String invalidRequestJson = "{"
                + "\"email\":\"invalid-email\""
                + "}";

        mockMvc.perform(post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequestJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Name is required")))
                .andExpect(content().string(containsString("Invalid email format")))
                .andExpect(content().string(containsString("Password is required")))
                .andExpect(content().string(containsString("Role is required")));
    }

    @Test
    void testLoginUser_Success() throws Exception {
        // Register the user first
        String registerJson = "{"
                + "\"name\":\"Jane Doe\","
                + "\"email\":\"jane.doe@example.com\","
                + "\"password\":\"securePassword123\","
                + "\"role\":\"NGO\""
                + "}";

        mockMvc.perform(post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerJson))
                .andExpect(status().isCreated());

        // Perform login
        String loginJson = "{"
                + "\"email\":\"jane.doe@example.com\","
                + "\"password\":\"securePassword123\""
                + "}";

        mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Login successful")))
                .andExpect(content().string(containsString("Jane Doe")))
                .andExpect(content().string(containsString("jane.doe@example.com")))
                .andExpect(content().string(containsString("NGO")));
    }

    @Test
    void testLoginUser_WrongPassword() throws Exception {
        // Register the user first
        String registerJson = "{"
                + "\"name\":\"Jane Doe\","
                + "\"email\":\"jane.doe@example.com\","
                + "\"password\":\"securePassword123\","
                + "\"role\":\"NGO\""
                + "}";

        mockMvc.perform(post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerJson))
                .andExpect(status().isCreated());

        // Perform login with wrong password
        String loginJson = "{"
                + "\"email\":\"jane.doe@example.com\","
                + "\"password\":\"wrongPassword\""
                + "}";

        mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Invalid email or password")));
    }

    @Test
    void testLoginUser_UserNotFound() throws Exception {
        // Perform login for unregistered user
        String loginJson = "{"
                + "\"email\":\"nonexistent@example.com\","
                + "\"password\":\"somePassword\""
                + "}";

        mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Invalid email or password")));
    }

    @Test
    void testLoginUser_ValidationErrors() throws Exception {
        String invalidLoginJson = "{"
                + "\"email\":\"not-an-email\""
                + "}";

        mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidLoginJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Invalid email format")))
                .andExpect(content().string(containsString("Password is required")));
    }
}
