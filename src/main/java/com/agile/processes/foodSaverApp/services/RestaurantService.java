package com.agile.processes.foodSaverApp.services;

import com.agile.processes.foodSaverApp.dtos.RestaurantRegisterRequestDTO;
import com.agile.processes.foodSaverApp.dtos.RestaurantResponseDTO;
import com.agile.processes.foodSaverApp.entities.Restaurant;
import com.agile.processes.foodSaverApp.entities.User;
import com.agile.processes.foodSaverApp.enums.Role;
import com.agile.processes.foodSaverApp.repository.RestaurantRepository;
import com.agile.processes.foodSaverApp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RestaurantService {

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public Restaurant registerRestaurant(RestaurantRegisterRequestDTO request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.getRole() != Role.RESTAURANT) {
            throw new IllegalArgumentException("User does not have RESTAURANT role");
        }

        if (restaurantRepository.existsByUser(user)) {
            throw new IllegalArgumentException("Restaurant is already registered for this user");
        }

        Restaurant restaurant = new Restaurant();
        restaurant.setUser(user);
        restaurant.setName(request.getName());
        restaurant.setAddress(request.getAddress());
        restaurant.setPhone(request.getPhone());

        return restaurantRepository.save(restaurant);
    }

    public List<RestaurantResponseDTO> getAllRestaurants() {
        return restaurantRepository.findAll().stream()
                .map(r -> new RestaurantResponseDTO(
                        r.getId(),
                        r.getUser().getId(),
                        r.getName(),
                        r.getAddress(),
                        r.getPhone(),
                        r.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }
}
