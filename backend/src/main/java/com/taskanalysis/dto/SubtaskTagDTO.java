package com.taskanalysis.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for SubtaskTag entity
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubtaskTagDTO {

    /**
     * Tag ID
     */
    private Long id;

    /**
     * Tag name (e.g., "Frontend", "Backend")
     */
    private String name;

    /**
     * Color in HEX format (e.g., "#667eea")
     */
    private String color;

    /**
     * Is this a global tag (visible to all)?
     */
    private Boolean isGlobal;

    /**
     * Owner user ID (NULL for global tags)
     */
    private Long userId;

    /**
     * Who created this tag (user ID)
     */
    private Long createdById;

    /**
     * When was this tag created
     */
    private LocalDateTime createdAt;

    /**
     * Number of subtasks using this tag (optional)
     */
    private Integer usageCount;
}
