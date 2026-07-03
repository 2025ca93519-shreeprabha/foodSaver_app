package com.agile.processes.foodSaverApp.controllers;

import com.agile.processes.foodSaverApp.dtos.PickupResponseDTO;
import com.agile.processes.foodSaverApp.dtos.RestaurantRegisterRequestDTO;
import com.agile.processes.foodSaverApp.dtos.RestaurantResponseDTO;
import com.agile.processes.foodSaverApp.services.PickupService;
import com.agile.processes.foodSaverApp.services.RestaurantService;
import com.agile.processes.foodSaverApp.entities.Restaurant;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class RestaurantController {

    @Autowired
    private RestaurantService restaurantService;

    @Autowired
    private PickupService pickupService;

    @PostMapping("/register/restaurant")
    public ResponseEntity<?> registerRestaurant(@Valid @RequestBody RestaurantRegisterRequestDTO request) {
        Restaurant restaurant = restaurantService.registerRestaurant(request);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Restaurant registered successfully");
        response.put("restaurantId", restaurant.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/restaurants")
    public ResponseEntity<List<RestaurantResponseDTO>> getAllRestaurants() {
        List<RestaurantResponseDTO> restaurants = restaurantService.getAllRestaurants();
        return ResponseEntity.ok(restaurants);
    }

    /**
     * View all pickup requests associated with a specific restaurant's food items.
     * GET /restaurants/{restaurantId}/pickups
     */
    @GetMapping("/restaurants/{restaurantId}/pickups")
    public ResponseEntity<List<PickupResponseDTO>> getPickupsByRestaurant(@PathVariable Long restaurantId) {
        List<PickupResponseDTO> pickups = pickupService.getPickupsByRestaurant(restaurantId);
        return ResponseEntity.ok(pickups);
    }

}
