package com.agile.processes.foodSaverApp.services;

import com.agile.processes.foodSaverApp.dtos.NotificationResponseDTO;
import com.agile.processes.foodSaverApp.entities.Notification;
import com.agile.processes.foodSaverApp.entities.Restaurant;
import com.agile.processes.foodSaverApp.repository.NotificationRepository;
import com.agile.processes.foodSaverApp.repository.RestaurantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    /**
     * Creates and saves a notification for a restaurant.
     */
    @Transactional
    public void createNotification(Restaurant restaurant, String message) {
        Notification notification = new Notification();
        notification.setRestaurant(restaurant);
        notification.setMessage(message);
        notification.setRead(false);
        notificationRepository.save(notification);
    }

    /**
     * Gets all notifications for a specific restaurant.
     */
    @Transactional(readOnly = true)
    public List<NotificationResponseDTO> getNotificationsForRestaurant(Long restaurantId) {
        if (!restaurantRepository.existsById(restaurantId)) {
            throw new IllegalArgumentException("Restaurant not found with id: " + restaurantId);
        }
        return notificationRepository.findByRestaurantIdOrderByCreatedAtDesc(restaurantId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Marks a notification as read.
     */
    @Transactional
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found with id: " + notificationId));
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    private NotificationResponseDTO mapToDTO(Notification notification) {
        return new NotificationResponseDTO(
                notification.getId(),
                notification.getRestaurant().getId(),
                notification.getRestaurant().getName(),
                notification.getMessage(),
                notification.isRead(),
                notification.getCreatedAt()
        );
    }
}
