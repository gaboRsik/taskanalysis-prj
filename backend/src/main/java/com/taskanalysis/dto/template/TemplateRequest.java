package com.taskanalysis.dto.template;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for creating or updating a task template
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TemplateRequest {

    @NotBlank(message = "Template name is required")
    @Size(max = 255, message = "Template name must not exceed 255 characters")
    private String name;

    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;

    @NotNull(message = "Category is required for analytics")
    private Long categoryId;

    @NotNull(message = "Subtask count is required")
    @Min(value = 1, message = "Subtask count must be at least 1")
    @Max(value = 100, message = "Subtask count must not exceed 100")
    private Integer subtaskCount;

    @Valid
    @Size(max = 100, message = "Template subtasks must not exceed 100")
    private List<TemplateSubtaskDTO> templateSubtasks;
}
