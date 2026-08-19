package com.agile.processes.foodSaverApp.services;

import com.agile.processes.foodSaverApp.dtos.RestaurantRegisterRequestDTO;
import com.agile.processes.foodSaverApp.dtos.RestaurantResponseDTO;
import com.agile.processes.foodSaverApp.entities.Restaurant;
import com.agile.processes.foodSaverApp.entities.User;
import com.agile.processes.foodSaverApp.enums.Role;
import com.agile.processes.foodSaverApp.repository.RestaurantRepository;
import com.agile.processes.foodSaverApp.repository.UserRepository;
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
public class RestaurantServiceTest {

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RestaurantService restaurantService;

    private User testUser;
    private RestaurantRegisterRequestDTO registerRequest;
    private Restaurant testRestaurant;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setRole(Role.RESTAURANT);

        registerRequest = new RestaurantRegisterRequestDTO();
        registerRequest.setUserId(1L);
        registerRequest.setName("Test Restaurant");
        registerRequest.setAddress("123 Street");
        registerRequest.setPhone("1234567890");

        testRestaurant = new Restaurant();
        testRestaurant.setId(10L);
        testRestaurant.setUser(testUser);
        testRestaurant.setName("Test Restaurant");
        testRestaurant.setAddress("123 Street");
        testRestaurant.setPhone("1234567890");
    }

    @Test
    void registerRestaurant_Success() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(restaurantRepository.existsByUser(any(User.class))).thenReturn(false);
        when(restaurantRepository.save(any(Restaurant.class))).thenReturn(testRestaurant);

        Restaurant savedRestaurant = restaurantService.registerRestaurant(registerRequest);

        assertNotNull(savedRestaurant);
        assertEquals("Test Restaurant", savedRestaurant.getName());
        verify(restaurantRepository, times(1)).save(any(Restaurant.class));
    }

    @Test
    void registerRestaurant_UserNotFound() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            restaurantService.registerRestaurant(registerRequest);
        });

        assertEquals("User not found", exception.getMessage());
        verify(restaurantRepository, never()).save(any(Restaurant.class));
    }

    @Test
    void registerRestaurant_UserNotRestaurantRole() {
        testUser.setRole(Role.NGO);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            restaurantService.registerRestaurant(registerRequest);
        });

        assertEquals("User does not have RESTAURANT role", exception.getMessage());
        verify(restaurantRepository, never()).save(any(Restaurant.class));
    }

    @Test
    void registerRestaurant_AlreadyRegistered() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(restaurantRepository.existsByUser(any(User.class))).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            restaurantService.registerRestaurant(registerRequest);
        });

        assertEquals("Restaurant is already registered for this user", exception.getMessage());
        verify(restaurantRepository, never()).save(any(Restaurant.class));
    }

    @Test
    void getAllRestaurants_Success() {
        Restaurant anotherRestaurant = new Restaurant();
        anotherRestaurant.setId(20L);
        User anotherUser = new User();
        anotherUser.setId(2L);
        anotherRestaurant.setUser(anotherUser);

        when(restaurantRepository.findAll()).thenReturn(Arrays.asList(testRestaurant, anotherRestaurant));

        List<RestaurantResponseDTO> responses = restaurantService.getAllRestaurants();

        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals(10L, responses.get(0).getId());
        assertEquals(20L, responses.get(1).getId());
    }
}
