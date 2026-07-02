package com.agile.processes.foodSaverApp.controllers;

import com.agile.processes.foodSaverApp.entities.Food;
import com.agile.processes.foodSaverApp.entities.Restaurant;
import com.agile.processes.foodSaverApp.entities.User;
import com.agile.processes.foodSaverApp.enums.FoodStatus;
import com.agile.processes.foodSaverApp.enums.Role;
import com.agile.processes.foodSaverApp.repository.FoodRepository;
import com.agile.processes.foodSaverApp.repository.RestaurantRepository;
import com.agile.processes.foodSaverApp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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
public class FoodControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private FoodRepository foodRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Restaurant restaurant;

    @BeforeEach
    void setUp() {
        foodRepository.deleteAll();
        restaurantRepository.deleteAll();
        userRepository.deleteAll();

        // Setup a test restaurant user
        User user = new User();
        user.setName("Test Restaurant Owner");
        user.setEmail("restaurant@example.com");
        user.setPassword(passwordEncoder.encode("password123"));
        user.setRole(Role.RESTAURANT);
        User savedUser = userRepository.save(user);

        // Setup a test restaurant
        Restaurant rest = new Restaurant();
        rest.setUser(savedUser);
        rest.setName("Delicious Diner");
        rest.setAddress("123 Food Street");
        rest.setPhone("555-0100");
        restaurant = restaurantRepository.save(rest);
    }

    @Test
    void testAddFood_Success() throws Exception {
        String requestJson = "{"
                + "\"restaurantId\":" + restaurant.getId() + ","
                + "\"name\":\"Paneer Butter Masala\","
                + "\"description\":\"Fresh paneer curry, serves 5\","
                + "\"quantity\":5,"
                + "\"quantityUnit\":\"servings\","
                + "\"expiryTime\":\"2026-07-29T23:59:59\""
                + "}";

        mockMvc.perform(post("/food")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Paneer Butter Masala"))
                .andExpect(jsonPath("$.quantity").value(5))
                .andExpect(jsonPath("$.status").value("AVAILABLE"))
                .andExpect(jsonPath("$.restaurantName").value("Delicious Diner"));

        List<Food> foods = foodRepository.findAll();
        assertEquals(1, foods.size());
        assertEquals("Paneer Butter Masala", foods.get(0).getName());
        assertEquals(FoodStatus.AVAILABLE, foods.get(0).getStatus());
    }

    @Test
    void testAddFood_RestaurantNotFound() throws Exception {
        String requestJson = "{"
                + "\"restaurantId\":99999,"
                + "\"name\":\"Paneer Butter Masala\","
                + "\"description\":\"Fresh paneer curry, serves 5\","
                + "\"quantity\":5,"
                + "\"quantityUnit\":\"servings\","
                + "\"expiryTime\":\"2026-07-29T23:59:59\""
                + "}";

        mockMvc.perform(post("/food")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Restaurant not found")));
    }

    @Test
    void testAddFood_ValidationErrors() throws Exception {
        String requestJson = "{"
                + "\"restaurantId\":null,"
                + "\"name\":\"\","
                + "\"quantity\":0,"
                + "\"quantityUnit\":\"\","
                + "\"expiryTime\":null"
                + "}";

        mockMvc.perform(post("/food")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Restaurant ID is required")))
                .andExpect(content().string(containsString("Food name is required")))
                .andExpect(content().string(containsString("Quantity must be at least 1")))
                .andExpect(content().string(containsString("Quantity unit is required")))
                .andExpect(content().string(containsString("Expiry time is required")));
    }

    @Test
    void testViewFood_Success() throws Exception {
        // Save available food
        Food food1 = new Food();
        food1.setRestaurant(restaurant);
        food1.setName("Veg Biryani");
        food1.setDescription("Serves 10");
        food1.setQuantity(10);
        food1.setQuantityUnit("servings");
        food1.setExpiryTime(LocalDateTime.of(2026, 7, 30, 12, 0));
        food1.setStatus(FoodStatus.AVAILABLE);
        foodRepository.save(food1);

        // Save claimed food (should not be returned)
        Food food2 = new Food();
        food2.setRestaurant(restaurant);
        food2.setName("Chicken Biryani");
        food2.setDescription("Serves 5");
        food2.setQuantity(5);
        food2.setQuantityUnit("servings");
        food2.setExpiryTime(LocalDateTime.of(2026, 7, 30, 12, 0));
        food2.setStatus(FoodStatus.CLAIMED);
        foodRepository.save(food2);

        mockMvc.perform(get("/food"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Veg Biryani"))
                .andExpect(jsonPath("$[0].status").value("AVAILABLE"));
    }

    @Test
    void testGetFoodByRestaurant_Success() throws Exception {
        // Save both available and claimed food for the restaurant
        Food food1 = new Food();
        food1.setRestaurant(restaurant);
        food1.setName("Veg Biryani");
        food1.setDescription("Serves 10");
        food1.setQuantity(10);
        food1.setQuantityUnit("servings");
        food1.setExpiryTime(LocalDateTime.of(2026, 7, 30, 12, 0));
        food1.setStatus(FoodStatus.AVAILABLE);
        foodRepository.save(food1);

        Food food2 = new Food();
        food2.setRestaurant(restaurant);
        food2.setName("Chicken Biryani");
        food2.setDescription("Serves 5");
        food2.setQuantity(5);
        food2.setQuantityUnit("servings");
        food2.setExpiryTime(LocalDateTime.of(2026, 7, 30, 12, 0));
        food2.setStatus(FoodStatus.CLAIMED);
        foodRepository.save(food2);

        mockMvc.perform(get("/restaurants/" + restaurant.getId() + "/food"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("Veg Biryani"))
                .andExpect(jsonPath("$[1].name").value("Chicken Biryani"));
    }

    @Test
    void testGetFoodByRestaurant_NotFound() throws Exception {
        mockMvc.perform(get("/restaurants/99999/food"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Restaurant not found")));
    }
}
