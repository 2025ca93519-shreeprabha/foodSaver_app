package com.agile.processes.foodSaverApp.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NGOResponseDTO {
    private Long id;
    private Long userId;
    private String name;
    private String address;
    private String phone;
    private LocalDateTime createdAt;
}
