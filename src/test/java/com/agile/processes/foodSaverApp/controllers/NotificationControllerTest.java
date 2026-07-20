package com.agile.processes.foodSaverApp.controllers;

import com.agile.processes.foodSaverApp.entities.*;
import com.agile.processes.foodSaverApp.enums.FoodStatus;
import com.agile.processes.foodSaverApp.enums.Role;
import com.agile.processes.foodSaverApp.repository.*;
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

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
public class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NGORepository ngoRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private FoodRepository foodRepository;

    @Autowired
    private PickupRequestRepository pickupRequestRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private NGO ngo;
    private Restaurant restaurant;
    private Food food;

    @BeforeEach
    void setUp() {
        notificationRepository.deleteAll();
        pickupRequestRepository.deleteAll();
        foodRepository.deleteAll();
        ngoRepository.deleteAll();
        restaurantRepository.deleteAll();
        userRepository.deleteAll();

        // 1. Setup NGO User & NGO
        User user1 = new User();
        user1.setName("NGO Owner");
        user1.setEmail("ngo@test.com");
        user1.setPassword(passwordEncoder.encode("password"));
        user1.setRole(Role.NGO);
        User savedNgoUser = userRepository.save(user1);

        ngo = new NGO();
        ngo.setUser(savedNgoUser);
        ngo.setName("Save Food NGO");
        ngo.setAddress("NGO Street 12");
        ngo.setPhone("1234567890");
        ngo = ngoRepository.save(ngo);

        // 2. Setup Restaurant User & Restaurant
        User user2 = new User();
        user2.setName("Restaurant Owner");
        user2.setEmail("restaurant@test.com");
        user2.setPassword(passwordEncoder.encode("password"));
        user2.setRole(Role.RESTAURANT);
        User savedRestUser = userRepository.save(user2);

        restaurant = new Restaurant();
        restaurant.setUser(savedRestUser);
        restaurant.setName("Delicious Pasta Place");
        restaurant.setAddress("Food Boulevard 45");
        restaurant.setPhone("0987654321");
        restaurant = restaurantRepository.save(restaurant);

        // 3. Setup Food item
        food = new Food();
        food.setRestaurant(restaurant);
        food.setName("Spaghetti");
        food.setDescription("Leftover warm spaghetti");
        food.setQuantity(10);
        food.setQuantityUnit("portions");
        food.setExpiryTime(LocalDateTime.now().plusHours(3));
        food.setStatus(FoodStatus.AVAILABLE);
        food = foodRepository.save(food);
    }

    @Test
    void testGetNotifications_Empty() throws Exception {
        mockMvc.perform(get("/restaurants/" + restaurant.getId() + "/notifications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void testGetNotifications_RestaurantNotFound() throws Exception {
        mockMvc.perform(get("/restaurants/99999/notifications"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testRequestPickup_CreatesNotification() throws Exception {
        String pickupRequestJson = "{"
                + "\"ngoId\":" + ngo.getId() + ","
                + "\"foodId\":" + food.getId() + ","
                + "\"requestedQuantity\":3"
                + "}";

        // Request pickup
        mockMvc.perform(post("/ngo/pickup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(pickupRequestJson))
                .andExpect(status().isCreated());

        // Verify notification is created for the restaurant
        List<Notification> notifications = notificationRepository.findByRestaurantIdOrderByCreatedAtDesc(restaurant.getId());
        assertEquals(1, notifications.size());
        
        Notification notification = notifications.get(0);
        assertEquals(restaurant.getId(), notification.getRestaurant().getId());
        assertFalse(notification.isRead());
        assertTrue(notification.getMessage().contains("Save Food NGO requested a pickup of 3 portions of Spaghetti"));

        // Fetch via controller
        mockMvc.perform(get("/restaurants/" + restaurant.getId() + "/notifications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].message", is(notification.getMessage())))
                .andExpect(jsonPath("$[0].read", is(false)));
    }

    @Test
    void testMarkNotificationAsRead() throws Exception {
        // Direct save a notification
        Notification notification = new Notification();
        notification.setRestaurant(restaurant);
        notification.setMessage("Test Message");
        notification.setRead(false);
        notification = notificationRepository.save(notification);

        // Mark it as read
        mockMvc.perform(put("/notifications/" + notification.getId() + "/read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is("Notification marked as read")));

        // Verify DB update
        Notification updatedNotification = notificationRepository.findById(notification.getId()).orElseThrow();
        assertTrue(updatedNotification.isRead());

        // Check via GET endpoint
        mockMvc.perform(get("/restaurants/" + restaurant.getId() + "/notifications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].read", is(true)));
    }

    @Test
    void testMarkNotificationAsRead_NotFound() throws Exception {
        mockMvc.perform(put("/notifications/99999/read"))
                .andExpect(status().isBadRequest());
    }
}
