package com.taskanalysis.dto.template;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for template subtask information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TemplateSubtaskDTO {
    
    private Integer subtaskNumber;
    private Integer plannedPoints;
}
