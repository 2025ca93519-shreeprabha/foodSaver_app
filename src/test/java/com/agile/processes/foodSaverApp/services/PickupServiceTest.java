package com.agile.processes.foodSaverApp.services;

import com.agile.processes.foodSaverApp.dtos.PickupRequestDTO;
import com.agile.processes.foodSaverApp.dtos.PickupResponseDTO;
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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PickupServiceTest {

    @Mock
    private PickupRequestRepository pickupRequestRepository;

    @Mock
    private NGORepository ngoRepository;

    @Mock
    private FoodRepository foodRepository;

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private PickupService pickupService;

    private NGO testNgo;
    private Food testFood;
    private Restaurant testRestaurant;
    private PickupRequestDTO pickupRequestDTO;
    private PickupRequest testPickupRequest;

    @BeforeEach
    void setUp() {
        testNgo = new NGO();
        testNgo.setId(1L);
        testNgo.setName("Test NGO");

        testRestaurant = new Restaurant();
        testRestaurant.setId(2L);
        testRestaurant.setName("Test Restaurant");

        testFood = new Food();
        testFood.setId(3L);
        testFood.setName("Rice");
        testFood.setQuantity(10);
        testFood.setQuantityUnit("kg");
        testFood.setStatus(FoodStatus.AVAILABLE);
        testFood.setRestaurant(testRestaurant);

        pickupRequestDTO = new PickupRequestDTO();
        pickupRequestDTO.setNgoId(1L);
        pickupRequestDTO.setFoodId(3L);
        pickupRequestDTO.setRequestedQuantity(5);

        testPickupRequest = new PickupRequest();
        testPickupRequest.setId(4L);
        testPickupRequest.setNgo(testNgo);
        testPickupRequest.setFood(testFood);
        testPickupRequest.setRequestedQuantity(5);
        testPickupRequest.setStatus(PickupStatus.PENDING);
    }

    @Test
    void requestPickup_Success() {
        when(ngoRepository.findById(anyLong())).thenReturn(Optional.of(testNgo));
        when(foodRepository.findById(anyLong())).thenReturn(Optional.of(testFood));
        when(pickupRequestRepository.save(any(PickupRequest.class))).thenReturn(testPickupRequest);

        PickupResponseDTO response = pickupService.requestPickup(pickupRequestDTO);

        assertNotNull(response);
        assertEquals(5, testFood.getQuantity()); // 10 - 5
        assertEquals(FoodStatus.AVAILABLE, testFood.getStatus());
        verify(foodRepository, times(1)).save(any(Food.class));
        verify(notificationService, times(1)).createNotification(any(), anyString());
    }

    @Test
    void requestPickup_FullQuantityClaimed() {
        pickupRequestDTO.setRequestedQuantity(10);
        testPickupRequest.setRequestedQuantity(10);

        when(ngoRepository.findById(anyLong())).thenReturn(Optional.of(testNgo));
        when(foodRepository.findById(anyLong())).thenReturn(Optional.of(testFood));
        when(pickupRequestRepository.save(any(PickupRequest.class))).thenReturn(testPickupRequest);

        PickupResponseDTO response = pickupService.requestPickup(pickupRequestDTO);

        assertNotNull(response);
        assertEquals(0, testFood.getQuantity());
        assertEquals(FoodStatus.CLAIMED, testFood.getStatus());
    }

    @Test
    void requestPickup_NgoNotFound() {
        when(ngoRepository.findById(anyLong())).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            pickupService.requestPickup(pickupRequestDTO);
        });

        assertEquals("NGO not found with id: 1", exception.getMessage());
    }

    @Test
    void requestPickup_FoodNotAvailable() {
        testFood.setStatus(FoodStatus.CLAIMED);
        when(ngoRepository.findById(anyLong())).thenReturn(Optional.of(testNgo));
        when(foodRepository.findById(anyLong())).thenReturn(Optional.of(testFood));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            pickupService.requestPickup(pickupRequestDTO);
        });

        assertEquals("Food item is no longer available for pickup", exception.getMessage());
    }

    @Test
    void getPickupsByNgo_Success() {
        when(ngoRepository.existsById(anyLong())).thenReturn(true);
        when(pickupRequestRepository.findByNgoId(anyLong())).thenReturn(Arrays.asList(testPickupRequest));

        List<PickupResponseDTO> responses = pickupService.getPickupsByNgo(1L);

        assertEquals(1, responses.size());
        assertEquals(4L, responses.get(0).getId());
    }

    @Test
    void updateStatusByRestaurant_Success() {
        when(restaurantRepository.existsById(anyLong())).thenReturn(true);
        when(pickupRequestRepository.findById(anyLong())).thenReturn(Optional.of(testPickupRequest));
        when(pickupRequestRepository.save(any(PickupRequest.class))).thenReturn(testPickupRequest);

        PickupResponseDTO response = pickupService.updateStatusByRestaurant(2L, 4L, PickupStatus.COMPLETED);

        assertNotNull(response);
        assertEquals(PickupStatus.COMPLETED, testPickupRequest.getStatus());
    }

    @Test
    void updateStatusByRestaurant_Cancelled() {
        testFood.setStatus(FoodStatus.CLAIMED);
        testFood.setQuantity(0);

        when(restaurantRepository.existsById(anyLong())).thenReturn(true);
        when(pickupRequestRepository.findById(anyLong())).thenReturn(Optional.of(testPickupRequest));
        when(pickupRequestRepository.save(any(PickupRequest.class))).thenReturn(testPickupRequest);

        PickupResponseDTO response = pickupService.updateStatusByRestaurant(2L, 4L, PickupStatus.CANCELLED);

        assertNotNull(response);
        assertEquals(PickupStatus.CANCELLED, testPickupRequest.getStatus());
        assertEquals(5, testFood.getQuantity());
        assertEquals(FoodStatus.AVAILABLE, testFood.getStatus());
    }

    @Test
    void updateStatusByNGO_Cancelled() {
        testFood.setStatus(FoodStatus.CLAIMED);
        testFood.setQuantity(0);

        when(ngoRepository.existsById(anyLong())).thenReturn(true);
        when(pickupRequestRepository.findById(anyLong())).thenReturn(Optional.of(testPickupRequest));
        when(pickupRequestRepository.save(any(PickupRequest.class))).thenReturn(testPickupRequest);

        PickupResponseDTO response = pickupService.updateStatusByNGO(1L, 4L, PickupStatus.CANCELLED);

        assertNotNull(response);
        assertEquals(PickupStatus.CANCELLED, testPickupRequest.getStatus());
        assertEquals(5, testFood.getQuantity());
        assertEquals(FoodStatus.AVAILABLE, testFood.getStatus());
        verify(notificationService, times(1)).createNotification(any(), anyString());
    }
}
