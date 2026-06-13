package com.agile.processes.foodSaverApp.controllers;

import com.agile.processes.foodSaverApp.dtos.NGORegisterRequestDTO;
import com.agile.processes.foodSaverApp.services.NGOService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class NGOController {

    @Autowired
    private NGOService ngoService;

    @PostMapping("/register/ngo")
    public ResponseEntity<?> registerNGO(@Valid @RequestBody NGORegisterRequestDTO request) {
        ngoService.registerNGO(request);
        return ResponseEntity.status(HttpStatus.CREATED).body("NGO registered successfully");
    }
}
