package com.agile.processes.foodSaverApp.repository;

import com.agile.processes.foodSaverApp.entities.Restaurant;
import com.agile.processes.foodSaverApp.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    boolean existsByUser(User user);
    Optional<Restaurant> findByUser(User user);
}
