package com.agile.processes.foodSaverApp.controllers;

import com.agile.processes.foodSaverApp.dtos.NGORegisterRequestDTO;
import com.agile.processes.foodSaverApp.dtos.PickupRequestDTO;
import com.agile.processes.foodSaverApp.dtos.PickupResponseDTO;
import com.agile.processes.foodSaverApp.dtos.NGOResponseDTO;
import com.agile.processes.foodSaverApp.services.NGOService;
import com.agile.processes.foodSaverApp.services.PickupService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class NGOController {

    @Autowired
    private NGOService ngoService;

    @Autowired
    private PickupService pickupService;

    @PostMapping("/register/ngo")
    public ResponseEntity<?> registerNGO(@Valid @RequestBody NGORegisterRequestDTO request) {
        ngoService.registerNGO(request);
        return ResponseEntity.status(HttpStatus.CREATED).body("NGO registered successfully");
    }

    /**
     * NGO selects a food donation for pickup with a specified quantity.
     * POST /ngo/pickup
     */
    // Note: To view available food/donations, use GET /food or GET /restaurants/{restaurantId}/food
    @PostMapping("/ngo/pickup")
    public ResponseEntity<PickupResponseDTO> requestPickup(@Valid @RequestBody PickupRequestDTO request) {
        PickupResponseDTO response = pickupService.requestPickup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * View all pickup requests made by a specific NGO.
     * GET /ngo/{ngoId}/pickups
     */
    @GetMapping("/ngo/{ngoId}/pickups")
    public ResponseEntity<List<PickupResponseDTO>> getPickupsByNgo(@PathVariable Long ngoId) {
        List<PickupResponseDTO> pickups = pickupService.getPickupsByNgo(ngoId);
        return ResponseEntity.ok(pickups);
    }

    /**
     * View all registered NGOs.
     * GET /ngos
     */
    @GetMapping("/ngos")
    public ResponseEntity<List<NGOResponseDTO>> getAllNgos() {
        List<NGOResponseDTO> ngos = ngoService.getAllNgos();
        return ResponseEntity.ok(ngos);
    }
}
