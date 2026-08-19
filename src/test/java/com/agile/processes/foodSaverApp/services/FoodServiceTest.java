package com.agile.processes.foodSaverApp.services;

import com.agile.processes.foodSaverApp.dtos.FoodRequestDTO;
import com.agile.processes.foodSaverApp.dtos.FoodResponseDTO;
import com.agile.processes.foodSaverApp.entities.Food;
import com.agile.processes.foodSaverApp.entities.Restaurant;
import com.agile.processes.foodSaverApp.enums.FoodStatus;
import com.agile.processes.foodSaverApp.repository.FoodRepository;
import com.agile.processes.foodSaverApp.repository.RestaurantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FoodServiceTest {

    @Mock
    private FoodRepository foodRepository;

    @Mock
    private RestaurantRepository restaurantRepository;

    @InjectMocks
    private FoodService foodService;

    private Restaurant testRestaurant;
    private Food testFood;
    private FoodRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        testRestaurant = new Restaurant();
        testRestaurant.setId(1L);
        testRestaurant.setName("Test Restaurant");

        testFood = new Food();
        testFood.setId(10L);
        testFood.setRestaurant(testRestaurant);
        testFood.setName("Pasta");
        testFood.setDescription("Delicious");
        testFood.setQuantity(20);
        testFood.setQuantityUnit("kg");
        testFood.setExpiryTime(LocalDateTime.now().plusDays(2));
        testFood.setStatus(FoodStatus.AVAILABLE);

        requestDTO = new FoodRequestDTO();
        requestDTO.setRestaurantId(1L);
        requestDTO.setName("Pasta");
        requestDTO.setDescription("Delicious");
        requestDTO.setQuantity(20);
        requestDTO.setQuantityUnit("kg");
        requestDTO.setExpiryTime(LocalDateTime.now().plusDays(2));
    }

    @Test
    void addFood_Success() {
        when(restaurantRepository.findById(anyLong())).thenReturn(Optional.of(testRestaurant));
        when(foodRepository.save(any(Food.class))).thenReturn(testFood);

        FoodResponseDTO response = foodService.addFood(requestDTO);

        assertNotNull(response);
        assertEquals("Pasta", response.getName());
        assertEquals("Test Restaurant", response.getRestaurantName());
        assertEquals(FoodStatus.AVAILABLE, response.getStatus());
        verify(foodRepository, times(1)).save(any(Food.class));
    }

    @Test
    void addFood_RestaurantNotFound() {
        when(restaurantRepository.findById(anyLong())).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            foodService.addFood(requestDTO);
        });

        assertEquals("Restaurant not found", exception.getMessage());
        verify(foodRepository, never()).save(any(Food.class));
    }

    @Test
    void getAllAvailableFood_Success() {
        when(foodRepository.findByStatus(FoodStatus.AVAILABLE)).thenReturn(Arrays.asList(testFood));

        List<FoodResponseDTO> responses = foodService.getAllAvailableFood();

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(10L, responses.get(0).getId());
    }

    @Test
    void getFoodByRestaurant_Success() {
        when(restaurantRepository.existsById(anyLong())).thenReturn(true);
        when(foodRepository.findByRestaurantId(anyLong())).thenReturn(Arrays.asList(testFood));

        List<FoodResponseDTO> responses = foodService.getFoodByRestaurant(1L);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(10L, responses.get(0).getId());
    }

    @Test
    void getFoodByRestaurant_RestaurantNotFound() {
        when(restaurantRepository.existsById(anyLong())).thenReturn(false);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            foodService.getFoodByRestaurant(1L);
        });

        assertEquals("Restaurant not found", exception.getMessage());
    }
}
