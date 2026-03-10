package com.taskanalysis.dto.subtask;

import com.taskanalysis.entity.Subtask;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubtaskResponse {

    private Long id;
    private Long taskId;
    private Integer subtaskNumber;
    private Integer plannedPoints;
    private Integer actualPoints;
    private Subtask.SubtaskStatus status;
    private Long totalTimeSeconds;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ===== Computed Metrics =====
    private Integer proportionalPlannedTimeMinutes;
    private Double plannedEfficiencyScore;
    private Double actualEfficiencyScore;
    private Double plannedTimePerPoint;
    private Double actualTimePerPoint;
    private Double efficiencyVariancePercent;
    private Double timeVariancePercent;

}
