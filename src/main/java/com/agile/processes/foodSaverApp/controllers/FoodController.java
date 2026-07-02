package com.agile.processes.foodSaverApp.controllers;

import com.agile.processes.foodSaverApp.dtos.FoodRequestDTO;
import com.agile.processes.foodSaverApp.dtos.FoodResponseDTO;
import com.agile.processes.foodSaverApp.services.FoodService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class FoodController {

    @Autowired
    private FoodService foodService;

    @PostMapping("/food")
    public ResponseEntity<FoodResponseDTO> addFood(@Valid @RequestBody FoodRequestDTO request) {
        FoodResponseDTO response = foodService.addFood(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/food")
    public ResponseEntity<List<FoodResponseDTO>> viewFood() {
        List<FoodResponseDTO> response = foodService.getAllAvailableFood();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/restaurants/{restaurantId}/food")
    public ResponseEntity<List<FoodResponseDTO>> getFoodByRestaurant(@PathVariable Long restaurantId) {
        List<FoodResponseDTO> response = foodService.getFoodByRestaurant(restaurantId);
        return ResponseEntity.ok(response);
    }
}
