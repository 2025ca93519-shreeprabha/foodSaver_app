package com.agile.processes.foodSaverApp.services;

import com.agile.processes.foodSaverApp.dtos.FoodRequestDTO;
import com.agile.processes.foodSaverApp.dtos.FoodResponseDTO;
import com.agile.processes.foodSaverApp.entities.Food;
import com.agile.processes.foodSaverApp.entities.Restaurant;
import com.agile.processes.foodSaverApp.enums.FoodStatus;
import com.agile.processes.foodSaverApp.repository.FoodRepository;
import com.agile.processes.foodSaverApp.repository.RestaurantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FoodService {

    @Autowired
    private FoodRepository foodRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Transactional
    public FoodResponseDTO addFood(FoodRequestDTO request) {
        Restaurant restaurant = restaurantRepository.findById(request.getRestaurantId())
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));

        Food food = new Food();
        food.setRestaurant(restaurant);
        food.setName(request.getName());
        food.setDescription(request.getDescription());
        food.setQuantity(request.getQuantity());
        food.setQuantityUnit(request.getQuantityUnit());
        food.setExpiryTime(request.getExpiryTime());
        food.setStatus(FoodStatus.AVAILABLE);

        Food savedFood = foodRepository.save(food);
        return mapToResponseDTO(savedFood);
    }

    @Transactional(readOnly = true)
    public List<FoodResponseDTO> getAllAvailableFood() {
        List<Food> foods = foodRepository.findByStatus(FoodStatus.AVAILABLE);
        return foods.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FoodResponseDTO> getFoodByRestaurant(Long restaurantId) {
        if (!restaurantRepository.existsById(restaurantId)) {
            throw new IllegalArgumentException("Restaurant not found");
        }
        List<Food> foods = foodRepository.findByRestaurantId(restaurantId);
        return foods.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    private FoodResponseDTO mapToResponseDTO(Food food) {
        return new FoodResponseDTO(
                food.getId(),
                food.getRestaurant().getId(),
                food.getRestaurant().getName(),
                food.getName(),
                food.getDescription(),
                food.getQuantity(),
                food.getQuantityUnit(),
                food.getExpiryTime(),
                food.getStatus(),
                food.getCreatedAt()
        );
    }
}
