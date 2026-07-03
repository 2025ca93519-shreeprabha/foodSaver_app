package com.agile.processes.foodSaverApp.services;

import com.agile.processes.foodSaverApp.dtos.LoginRequestDTO;
import com.agile.processes.foodSaverApp.dtos.LoginResponseDTO;
import com.agile.processes.foodSaverApp.dtos.RegisterRequestDTO;
import com.agile.processes.foodSaverApp.entities.User;
import com.agile.processes.foodSaverApp.enums.Role;
import com.agile.processes.foodSaverApp.repository.NGORepository;
import com.agile.processes.foodSaverApp.repository.RestaurantRepository;
import com.agile.processes.foodSaverApp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private NGORepository ngoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User registerUser(RegisterRequestDTO request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already in use");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());

        return userRepository.save(user);
    }

    public LoginResponseDTO loginUser(LoginRequestDTO request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        Long restaurantId = null;
        Long ngoId = null;

        if (user.getRole() == Role.RESTAURANT) {
            restaurantId = restaurantRepository.findByUser(user)
                    .map(restaurant -> restaurant.getId())
                    .orElse(null);
        } else if (user.getRole() == Role.NGO) {
            ngoId = ngoRepository.findByUser(user)
                    .map(ngo -> ngo.getId())
                    .orElse(null);
        }

        return new LoginResponseDTO(user.getId(), "Login successful", user.getName(), user.getEmail(), user.getRole(), restaurantId, ngoId);
    }
}
