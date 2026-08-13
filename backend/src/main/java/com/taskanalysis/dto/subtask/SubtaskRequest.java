package com.taskanalysis.dto.subtask;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.util.List;

@Data
public class SubtaskRequest {

    @Min(value = 0, message = "Planned points must be at least 0")
    @Max(value = 1000, message = "Planned points must not exceed 1000")
    private Integer plannedPoints;

    @Min(value = 0, message = "Actual points must be at least 0")
    @Max(value = 1000, message = "Actual points must not exceed 1000")
    private Integer actualPoints;

    /**
     * Tag IDs to associate with this subtask.
     * User can only assign tags that are visible to them (global + their own).
     */
    private List<Long> tagIds;

}
