package com.agile.processes.foodSaverApp.controllers;

import com.agile.processes.foodSaverApp.dtos.PickupResponseDTO;
import com.agile.processes.foodSaverApp.enums.PickupStatus;
import com.agile.processes.foodSaverApp.services.PickupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class PickupController {

    @Autowired
    private PickupService pickupService;

    /**
     * Restaurant updates the status of a pickup request (COMPLETED / CANCELLED).
     * PUT /restaurants/{restaurantId}/pickups/{pickupId}/status?status=COMPLETED|CANCELLED
     */
    @PutMapping("/restaurants/{restaurantId}/pickups/{pickupId}/status")
    public ResponseEntity<PickupResponseDTO> updateStatusByRestaurant(
            @PathVariable Long restaurantId,
            @PathVariable Long pickupId,
            @RequestParam PickupStatus status) {
        PickupResponseDTO response = pickupService.updateStatusByRestaurant(restaurantId, pickupId, status);
        return ResponseEntity.ok(response);
    }

    /**
     * NGO cancels a pickup request.
     * PUT /ngo/{ngoId}/pickups/{pickupId}/status?status=CANCELLED
     */
    @PutMapping("/ngo/{ngoId}/pickups/{pickupId}/status")
    public ResponseEntity<PickupResponseDTO> updateStatusByNGO(
            @PathVariable Long ngoId,
            @PathVariable Long pickupId,
            @RequestParam PickupStatus status) {
        PickupResponseDTO response = pickupService.updateStatusByNGO(ngoId, pickupId, status);
        return ResponseEntity.ok(response);
    }
}
