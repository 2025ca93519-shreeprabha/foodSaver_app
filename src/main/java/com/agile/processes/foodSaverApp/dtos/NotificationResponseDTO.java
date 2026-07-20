package com.agile.processes.foodSaverApp.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponseDTO {
    private Long id;
    private Long restaurantId;
    private String restaurantName;
    private String message;
    private boolean isRead;
    private LocalDateTime createdAt;
}
