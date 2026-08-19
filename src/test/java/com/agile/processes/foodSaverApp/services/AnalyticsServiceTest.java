package com.agile.processes.foodSaverApp.services;

import com.agile.processes.foodSaverApp.dtos.NGOAnalyticsDTO;
import com.agile.processes.foodSaverApp.dtos.RestaurantAnalyticsDTO;
import com.agile.processes.foodSaverApp.entities.Food;
import com.agile.processes.foodSaverApp.entities.NGO;
import com.agile.processes.foodSaverApp.entities.PickupRequest;
import com.agile.processes.foodSaverApp.entities.Restaurant;
import com.agile.processes.foodSaverApp.enums.FoodStatus;
import com.agile.processes.foodSaverApp.enums.PickupStatus;
import com.agile.processes.foodSaverApp.repository.FoodRepository;
import com.agile.processes.foodSaverApp.repository.NGORepository;
import com.agile.processes.foodSaverApp.repository.PickupRequestRepository;
import com.agile.processes.foodSaverApp.repository.RestaurantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AnalyticsServiceTest {

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private NGORepository ngoRepository;

    @Mock
    private FoodRepository foodRepository;

    @Mock
    private PickupRequestRepository pickupRequestRepository;

    @InjectMocks
    private AnalyticsService analyticsService;

    private Restaurant testRestaurant;
    private NGO testNgo;
    private Food testFood1;
    private Food testFood2;
    private PickupRequest testPickup1;
    private PickupRequest testPickup2;

    @BeforeEach
    void setUp() {
        testRestaurant = new Restaurant();
        testRestaurant.setId(1L);
        testRestaurant.setName("Test Restaurant");

        testNgo = new NGO();
        testNgo.setId(2L);
        testNgo.setName("Test NGO");

        testFood1 = new Food();
        testFood1.setId(10L);
        testFood1.setRestaurant(testRestaurant);
        testFood1.setQuantity(5);
        testFood1.setQuantityUnit("kg");
        testFood1.setStatus(FoodStatus.AVAILABLE);
        testFood1.setExpiryTime(LocalDateTime.now().plusDays(1));

        testFood2 = new Food();
        testFood2.setId(11L);
        testFood2.setRestaurant(testRestaurant);
        testFood2.setQuantity(0);
        testFood2.setQuantityUnit("kg");
        testFood2.setStatus(FoodStatus.CLAIMED);
        testFood2.setExpiryTime(LocalDateTime.now().plusDays(1));

        testPickup1 = new PickupRequest();
        testPickup1.setId(100L);
        testPickup1.setFood(testFood1);
        testPickup1.setNgo(testNgo);
        testPickup1.setRequestedQuantity(2);
        testPickup1.setStatus(PickupStatus.PENDING);

        testPickup2 = new PickupRequest();
        testPickup2.setId(101L);
        testPickup2.setFood(testFood2);
        testPickup2.setNgo(testNgo);
        testPickup2.setRequestedQuantity(5);
        testPickup2.setStatus(PickupStatus.COMPLETED);
    }

    @Test
    void getRestaurantAnalytics_Success() {
        when(restaurantRepository.findById(anyLong())).thenReturn(Optional.of(testRestaurant));
        when(foodRepository.findByRestaurantId(anyLong())).thenReturn(Arrays.asList(testFood1, testFood2));
        when(pickupRequestRepository.findByFoodRestaurantId(anyLong())).thenReturn(Arrays.asList(testPickup1, testPickup2));

        RestaurantAnalyticsDTO dto = analyticsService.getRestaurantAnalytics(1L);

        assertNotNull(dto);
        assertEquals(1L, dto.getRestaurantId());
        assertEquals(2, dto.getTotalDonationsCount());
        assertEquals(1, dto.getActiveDonationsCount());
        assertEquals(1, dto.getClaimedDonationsCount());
        assertEquals(2, dto.getTotalPickupsReceived());
        assertEquals(1, dto.getPendingPickupsCount());
        assertEquals(1, dto.getCompletedPickupsCount());
        
        // Quantities
        // posted = 5 (available) + 0 (claimed remaining) + 2 (pending requested) + 5 (completed requested) = 12 kg
        assertEquals(12, dto.getTotalQuantityPostedByUnit().get("kg"));
        assertEquals(7, dto.getTotalQuantityClaimedByUnit().get("kg")); // 2 + 5
        assertEquals(5, dto.getTotalQuantityCompletedByUnit().get("kg")); // 5
    }

    @Test
    void getNGOAnalytics_Success() {
        when(ngoRepository.findById(anyLong())).thenReturn(Optional.of(testNgo));
        when(pickupRequestRepository.findByNgoId(anyLong())).thenReturn(Arrays.asList(testPickup1, testPickup2));

        NGOAnalyticsDTO dto = analyticsService.getNGOAnalytics(2L);

        assertNotNull(dto);
        assertEquals(2L, dto.getNgoId());
        assertEquals(2, dto.getTotalPickupsRequested());
        assertEquals(1, dto.getPendingPickupsCount());
        assertEquals(1, dto.getCompletedPickupsCount());

        assertEquals(7, dto.getTotalQuantityClaimedByUnit().get("kg")); // 2 + 5
        assertEquals(5, dto.getTotalQuantityCompletedByUnit().get("kg")); // 5
    }
}
