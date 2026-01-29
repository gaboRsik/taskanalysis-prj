package com.taskanalysis.dto.task;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TaskRequest {

    @NotBlank(message = "Task name is required")
    @Size(min = 1, max = 255, message = "Task name must be between 1 and 255 characters")
    private String name;

    private String description;

    private Long categoryId;

    @NotNull(message = "Subtask count is required")
    @Min(value = 1, message = "Subtask count must be at least 1")
    private Integer subtaskCount;

}
