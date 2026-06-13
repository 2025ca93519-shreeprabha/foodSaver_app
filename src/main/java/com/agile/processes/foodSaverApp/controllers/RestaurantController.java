package com.agile.processes.foodSaverApp.controllers;

import com.agile.processes.foodSaverApp.dtos.RestaurantRegisterRequestDTO;
import com.agile.processes.foodSaverApp.services.RestaurantService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RestaurantController {

    @Autowired
    private RestaurantService restaurantService;

    @PostMapping("/register/restaurant")
    public ResponseEntity<?> registerRestaurant(@Valid @RequestBody RestaurantRegisterRequestDTO request) {
        restaurantService.registerRestaurant(request);
        return ResponseEntity.status(HttpStatus.CREATED).body("Restaurant registered successfully");
    }
}
