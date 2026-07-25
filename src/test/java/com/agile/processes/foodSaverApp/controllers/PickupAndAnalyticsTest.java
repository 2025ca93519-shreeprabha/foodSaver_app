package com.agile.processes.foodSaverApp.controllers;

import com.agile.processes.foodSaverApp.entities.*;
import com.agile.processes.foodSaverApp.enums.FoodStatus;
import com.agile.processes.foodSaverApp.enums.PickupStatus;
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

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
public class PickupAndAnalyticsTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private NGORepository ngoRepository;

    @Autowired
    private FoodRepository foodRepository;

    @Autowired
    private PickupRequestRepository pickupRequestRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Restaurant restaurant;
    private NGO ngo;
    private Food foodServings;
    private Food foodKgs;

    @BeforeEach
    void setUp() {
        notificationRepository.deleteAll();
        pickupRequestRepository.deleteAll();
        foodRepository.deleteAll();
        restaurantRepository.deleteAll();
        ngoRepository.deleteAll();
        userRepository.deleteAll();

        // 1. Setup Restaurant User & Restaurant
        User restUser = new User();
        restUser.setName("Test Restaurant Owner");
        restUser.setEmail("restaurant@example.com");
        restUser.setPassword(passwordEncoder.encode("password123"));
        restUser.setRole(Role.RESTAURANT);
        User savedRestUser = userRepository.save(restUser);

        Restaurant rest = new Restaurant();
        rest.setUser(savedRestUser);
        rest.setName("Delicious Diner");
        rest.setAddress("123 Food Street");
        rest.setPhone("555-0100");
        restaurant = restaurantRepository.save(rest);

        // 2. Setup NGO User & NGO
        User ngoUser = new User();
        ngoUser.setName("Test NGO Owner");
        ngoUser.setEmail("ngo@example.com");
        ngoUser.setPassword(passwordEncoder.encode("password123"));
        ngoUser.setRole(Role.NGO);
        User savedNgoUser = userRepository.save(ngoUser);

        NGO ngoEntity = new NGO();
        ngoEntity.setUser(savedNgoUser);
        ngoEntity.setName("Feed The Hungry");
        ngoEntity.setAddress("456 Care Lane");
        ngoEntity.setPhone("555-0200");
        ngo = ngoRepository.save(ngoEntity);

        // 3. Setup Food Items
        Food f1 = new Food();
        f1.setRestaurant(restaurant);
        f1.setName("Veg Thali");
        f1.setDescription("Fresh veg thalis");
        f1.setQuantity(20);
        f1.setQuantityUnit("servings");
        f1.setExpiryTime(LocalDateTime.now().plusDays(2));
        f1.setStatus(FoodStatus.AVAILABLE);
        foodServings = foodRepository.save(f1);

        Food f2 = new Food();
        f2.setRestaurant(restaurant);
        f2.setName("Rice Bags");
        f2.setDescription("Raw basmati rice");
        f2.setQuantity(10);
        f2.setQuantityUnit("kgs");
        f2.setExpiryTime(LocalDateTime.now().plusDays(10));
        f2.setStatus(FoodStatus.AVAILABLE);
        foodKgs = foodRepository.save(f2);
    }

    @Test
    void testUpdateStatusByRestaurant_CompleteSuccess() throws Exception {
        // Create a pickup request
        PickupRequest pr = new PickupRequest();
        pr.setNgo(ngo);
        pr.setFood(foodServings);
        pr.setRequestedQuantity(5);
        pr.setStatus(PickupStatus.PENDING);
        pr = pickupRequestRepository.save(pr);

        // Update status to COMPLETED
        mockMvc.perform(put("/restaurants/" + restaurant.getId() + "/pickups/" + pr.getId() + "/status")
                .param("status", "COMPLETED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        PickupRequest updated = pickupRequestRepository.findById(pr.getId()).orElseThrow();
        assertEquals(PickupStatus.COMPLETED, updated.getStatus());
    }

    @Test
    void testUpdateStatusByRestaurant_CancelSuccess() throws Exception {
        // Prepare: decrease food quantity representing reservation
        foodServings.setQuantity(15);
        foodRepository.save(foodServings);

        PickupRequest pr = new PickupRequest();
        pr.setNgo(ngo);
        pr.setFood(foodServings);
        pr.setRequestedQuantity(5);
        pr.setStatus(PickupStatus.PENDING);
        pr = pickupRequestRepository.save(pr);

        // Update status to CANCELLED
        mockMvc.perform(put("/restaurants/" + restaurant.getId() + "/pickups/" + pr.getId() + "/status")
                .param("status", "CANCELLED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        PickupRequest updated = pickupRequestRepository.findById(pr.getId()).orElseThrow();
        assertEquals(PickupStatus.CANCELLED, updated.getStatus());

        // Verify food quantity is restored
        Food updatedFood = foodRepository.findById(foodServings.getId()).orElseThrow();
        assertEquals(20, updatedFood.getQuantity());
    }

    @Test
    void testUpdateStatusByNGO_CancelSuccess() throws Exception {
        // Prepare: decrease food quantity representing reservation
        foodKgs.setQuantity(6);
        foodRepository.save(foodKgs);

        PickupRequest pr = new PickupRequest();
        pr.setNgo(ngo);
        pr.setFood(foodKgs);
        pr.setRequestedQuantity(4);
        pr.setStatus(PickupStatus.PENDING);
        pr = pickupRequestRepository.save(pr);

        // NGO cancels
        mockMvc.perform(put("/ngo/" + ngo.getId() + "/pickups/" + pr.getId() + "/status")
                .param("status", "CANCELLED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        PickupRequest updated = pickupRequestRepository.findById(pr.getId()).orElseThrow();
        assertEquals(PickupStatus.CANCELLED, updated.getStatus());

        // Verify food quantity is restored
        Food updatedFood = foodRepository.findById(foodKgs.getId()).orElseThrow();
        assertEquals(10, updatedFood.getQuantity());

        // Verify notification is created for restaurant
        List<Notification> notifications = notificationRepository.findAll();
        assertEquals(1, notifications.size());
        assertEquals(restaurant.getId(), notifications.get(0).getRestaurant().getId());
        assertEquals("NGO Feed The Hungry cancelled the pickup request for Rice Bags.", notifications.get(0).getMessage());
    }

    @Test
    void testUpdateStatusByNGO_CompleteFails() throws Exception {
        PickupRequest pr = new PickupRequest();
        pr.setNgo(ngo);
        pr.setFood(foodServings);
        pr.setRequestedQuantity(5);
        pr.setStatus(PickupStatus.PENDING);
        pr = pickupRequestRepository.save(pr);

        // NGO attempts to complete -> Should fail
        mockMvc.perform(put("/ngo/" + ngo.getId() + "/pickups/" + pr.getId() + "/status")
                .param("status", "COMPLETED"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetRestaurantAnalytics_Success() throws Exception {
        // Setup:
        // Food 1: servings. 10 remaining (10 claimed/pending/completed)
        // Food 2: kgs. 6 remaining (4 claimed/pending/completed)
        foodServings.setQuantity(10);
        foodRepository.save(foodServings);

        foodKgs.setQuantity(6);
        foodRepository.save(foodKgs);

        // Pickup 1: servings, PENDING, qty 4
        PickupRequest pr1 = new PickupRequest();
        pr1.setNgo(ngo);
        pr1.setFood(foodServings);
        pr1.setRequestedQuantity(4);
        pr1.setStatus(PickupStatus.PENDING);
        pickupRequestRepository.save(pr1);

        // Pickup 2: servings, COMPLETED, qty 6
        PickupRequest pr2 = new PickupRequest();
        pr2.setNgo(ngo);
        pr2.setFood(foodServings);
        pr2.setRequestedQuantity(6);
        pr2.setStatus(PickupStatus.COMPLETED);
        pickupRequestRepository.save(pr2);

        // Pickup 3: kgs, COMPLETED, qty 4
        PickupRequest pr3 = new PickupRequest();
        pr3.setNgo(ngo);
        pr3.setFood(foodKgs);
        pr3.setRequestedQuantity(4);
        pr3.setStatus(PickupStatus.COMPLETED);
        pickupRequestRepository.save(pr3);

        // Pickup 4: servings, CANCELLED, qty 5
        PickupRequest pr4 = new PickupRequest();
        pr4.setNgo(ngo);
        pr4.setFood(foodServings);
        pr4.setRequestedQuantity(5);
        pr4.setStatus(PickupStatus.CANCELLED);
        pickupRequestRepository.save(pr4);

        mockMvc.perform(get("/restaurants/" + restaurant.getId() + "/analytics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.restaurantId").value(restaurant.getId()))
                .andExpect(jsonPath("$.totalDonationsCount").value(2))
                .andExpect(jsonPath("$.totalPickupsReceived").value(4))
                .andExpect(jsonPath("$.pendingPickupsCount").value(1))
                .andExpect(jsonPath("$.completedPickupsCount").value(2))
                .andExpect(jsonPath("$.cancelledPickupsCount").value(1))
                .andExpect(jsonPath("$.totalQuantityPostedByUnit.servings").value(20))
                .andExpect(jsonPath("$.totalQuantityPostedByUnit.kgs").value(10))
                .andExpect(jsonPath("$.totalQuantityClaimedByUnit.servings").value(10))
                .andExpect(jsonPath("$.totalQuantityClaimedByUnit.kgs").value(4))
                .andExpect(jsonPath("$.totalQuantityCompletedByUnit.servings").value(6))
                .andExpect(jsonPath("$.totalQuantityCompletedByUnit.kgs").value(4));
    }

    @Test
    void testGetNGOAnalytics_Success() throws Exception {
        // Setup:
        // Pickup 1: servings, PENDING, qty 5
        PickupRequest pr1 = new PickupRequest();
        pr1.setNgo(ngo);
        pr1.setFood(foodServings);
        pr1.setRequestedQuantity(5);
        pr1.setStatus(PickupStatus.PENDING);
        pickupRequestRepository.save(pr1);

        // Pickup 2: kgs, COMPLETED, qty 3
        PickupRequest pr2 = new PickupRequest();
        pr2.setNgo(ngo);
        pr2.setFood(foodKgs);
        pr2.setRequestedQuantity(3);
        pr2.setStatus(PickupStatus.COMPLETED);
        pickupRequestRepository.save(pr2);

        mockMvc.perform(get("/ngo/" + ngo.getId() + "/analytics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ngoId").value(ngo.getId()))
                .andExpect(jsonPath("$.totalPickupsRequested").value(2))
                .andExpect(jsonPath("$.pendingPickupsCount").value(1))
                .andExpect(jsonPath("$.completedPickupsCount").value(1))
                .andExpect(jsonPath("$.totalQuantityClaimedByUnit.servings").value(5))
                .andExpect(jsonPath("$.totalQuantityClaimedByUnit.kgs").value(3))
                .andExpect(jsonPath("$.totalQuantityCompletedByUnit.kgs").value(3))
                .andExpect(jsonPath("$.totalQuantityCompletedByUnit.servings").doesNotExist());
    }
}
