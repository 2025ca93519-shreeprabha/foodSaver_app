package com.agile.processes.foodSaverApp.controllers;

import com.agile.processes.foodSaverApp.dtos.NotificationResponseDTO;
import com.agile.processes.foodSaverApp.services.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    /**
     * Get all notifications for a specific restaurant.
     * GET /restaurants/{restaurantId}/notifications
     */
    @GetMapping("/restaurants/{restaurantId}/notifications")
    public ResponseEntity<List<NotificationResponseDTO>> getNotificationsForRestaurant(@PathVariable Long restaurantId) {
        List<NotificationResponseDTO> notifications = notificationService.getNotificationsForRestaurant(restaurantId);
        return ResponseEntity.ok(notifications);
    }

    /**
     * Mark a notification as read.
     * PUT /notifications/{notificationId}/read
     */
    @PutMapping("/notifications/{notificationId}/read")
    public ResponseEntity<String> markAsRead(@PathVariable Long notificationId) {
        notificationService.markAsRead(notificationId);
        return ResponseEntity.ok("Notification marked as read");
    }
}
