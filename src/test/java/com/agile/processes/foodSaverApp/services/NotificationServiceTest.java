package com.agile.processes.foodSaverApp.services;

import com.agile.processes.foodSaverApp.dtos.NotificationResponseDTO;
import com.agile.processes.foodSaverApp.entities.Notification;
import com.agile.processes.foodSaverApp.entities.Restaurant;
import com.agile.processes.foodSaverApp.repository.NotificationRepository;
import com.agile.processes.foodSaverApp.repository.RestaurantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private RestaurantRepository restaurantRepository;

    @InjectMocks
    private NotificationService notificationService;

    private Restaurant testRestaurant;
    private Notification testNotification;

    @BeforeEach
    void setUp() {
        testRestaurant = new Restaurant();
        testRestaurant.setId(1L);
        testRestaurant.setName("Test Restaurant");

        testNotification = new Notification();
        testNotification.setId(10L);
        testNotification.setRestaurant(testRestaurant);
        testNotification.setMessage("Test message");
        testNotification.setRead(false);
    }

    @Test
    void createNotification_Success() {
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        notificationService.createNotification(testRestaurant, "Test message");

        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void getNotificationsForRestaurant_Success() {
        when(restaurantRepository.existsById(anyLong())).thenReturn(true);
        when(notificationRepository.findByRestaurantIdOrderByCreatedAtDesc(anyLong())).thenReturn(Arrays.asList(testNotification));

        List<NotificationResponseDTO> dtos = notificationService.getNotificationsForRestaurant(1L);

        assertNotNull(dtos);
        assertEquals(1, dtos.size());
        assertEquals(10L, dtos.get(0).getId());
        assertEquals("Test message", dtos.get(0).getMessage());
        assertFalse(dtos.get(0).isRead());
    }

    @Test
    void getNotificationsForRestaurant_RestaurantNotFound() {
        when(restaurantRepository.existsById(anyLong())).thenReturn(false);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            notificationService.getNotificationsForRestaurant(1L);
        });

        assertEquals("Restaurant not found with id: 1", exception.getMessage());
    }

    @Test
    void markAsRead_Success() {
        when(notificationRepository.findById(anyLong())).thenReturn(Optional.of(testNotification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        notificationService.markAsRead(10L);

        assertTrue(testNotification.isRead());
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void markAsRead_NotificationNotFound() {
        when(notificationRepository.findById(anyLong())).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            notificationService.markAsRead(10L);
        });

        assertEquals("Notification not found with id: 10", exception.getMessage());
    }
}
