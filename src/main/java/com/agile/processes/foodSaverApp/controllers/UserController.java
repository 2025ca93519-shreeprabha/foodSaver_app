package com.agile.processes.foodSaverApp.controllers;

import com.agile.processes.foodSaverApp.dtos.LoginRequestDTO;
import com.agile.processes.foodSaverApp.dtos.LoginResponseDTO;
import com.agile.processes.foodSaverApp.dtos.RegisterRequestDTO;
import com.agile.processes.foodSaverApp.services.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequestDTO registerRequest) {
        userService.registerUser(registerRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody LoginRequestDTO loginRequest) {
        LoginResponseDTO response = userService.loginUser(loginRequest);
        return ResponseEntity.ok(response);
    }
}
