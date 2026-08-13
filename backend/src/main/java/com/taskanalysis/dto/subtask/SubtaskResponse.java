package com.taskanalysis.dto.subtask;

import com.taskanalysis.dto.SubtaskTagDTO;
import com.taskanalysis.entity.Subtask;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

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
    
    // Tags associated with this subtask
    private List<SubtaskTagDTO> tags;

    // ===== Computed Metrics =====
    private Integer proportionalPlannedTimeMinutes;
    private Double plannedEfficiencyScore;
    private Double actualEfficiencyScore;
    private Double plannedTimePerPoint;
    private Double actualTimePerPoint;
    private Double efficiencyVariancePercent;
    private Double timeVariancePercent;

}
