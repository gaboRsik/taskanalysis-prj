package com.taskanalysis.dto.template;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for task template
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TemplateResponse {

    private Long id;
    private String name;
    private String description;
    private Long categoryId;
    private String categoryName;
    private Integer subtaskCount;
    private List<TemplateSubtaskDTO> templateSubtasks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
