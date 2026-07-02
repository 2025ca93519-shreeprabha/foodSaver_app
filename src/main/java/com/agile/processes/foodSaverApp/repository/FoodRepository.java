package com.agile.processes.foodSaverApp.repository;

import com.agile.processes.foodSaverApp.entities.Food;
import com.agile.processes.foodSaverApp.enums.FoodStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FoodRepository extends JpaRepository<Food, Long> {
    List<Food> findByStatus(FoodStatus status);
    List<Food> findByRestaurantId(Long restaurantId);
}
