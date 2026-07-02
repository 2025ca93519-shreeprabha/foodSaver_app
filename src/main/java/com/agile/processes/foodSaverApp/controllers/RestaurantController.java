package com.agile.processes.foodSaverApp.controllers;

import com.agile.processes.foodSaverApp.dtos.PickupResponseDTO;
import com.agile.processes.foodSaverApp.dtos.RestaurantRegisterRequestDTO;
import com.agile.processes.foodSaverApp.dtos.RestaurantResponseDTO;
import com.agile.processes.foodSaverApp.services.PickupService;
import com.agile.processes.foodSaverApp.services.RestaurantService;
import jakarta.validation.Valid;
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
        restaurantService.registerRestaurant(request);
        return ResponseEntity.status(HttpStatus.CREATED).body("Restaurant registered successfully");
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
