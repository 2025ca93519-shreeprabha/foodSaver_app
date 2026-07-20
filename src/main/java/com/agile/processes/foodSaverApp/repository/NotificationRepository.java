package com.agile.processes.foodSaverApp.repository;

import com.agile.processes.foodSaverApp.entities.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByRestaurantIdOrderByCreatedAtDesc(Long restaurantId);
}
