package com.taskanalysis.dto.template;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for template subtask information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TemplateSubtaskDTO {
    
    private Integer subtaskNumber;
    private Integer plannedPoints;
    
    /**
     * Tag IDs associated with this template subtask
     * (only global tags recommended for templates)
     */
    private List<Long> tagIds;
}
