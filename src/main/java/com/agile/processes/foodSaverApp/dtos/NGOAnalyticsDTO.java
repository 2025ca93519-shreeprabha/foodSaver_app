package com.agile.processes.foodSaverApp.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NGOAnalyticsDTO {
    private Long ngoId;
    private String ngoName;
    private long totalPickupsRequested;
    private long pendingPickupsCount;
    private long completedPickupsCount;
    private long cancelledPickupsCount;
    private Map<String, Integer> totalQuantityClaimedByUnit;
    private Map<String, Integer> totalQuantityCompletedByUnit;
}
