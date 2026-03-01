package com.taskanalysis.dto.task;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TaskUpdateRequest {

    @NotBlank(message = "Task name is required")
    @Size(min = 1, max = 255, message = "Task name must be between 1 and 255 characters")
    private String name;

    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;

    private Long categoryId;

}
