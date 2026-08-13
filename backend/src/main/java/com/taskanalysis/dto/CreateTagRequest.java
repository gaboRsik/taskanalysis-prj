package com.taskanalysis.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating or updating a subtask tag
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTagRequest {

    /**
     * Tag name
     * Required, max 50 characters
     */
    @NotBlank(message = "Tag name is required")
    @Size(max = 50, message = "Tag name must be at most 50 characters")
    private String name;

    /**
     * Color in HEX format (e.g., "#667eea")
     * Required, must be valid HEX color
     */
    @NotBlank(message = "Color is required")
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Color must be a valid HEX color (e.g., #667eea)")
    private String color;

    /**
     * Should this be a global tag?
     * Only admins can create global tags
     * Default: false (user-specific tag)
     */
    @JsonProperty("isGlobal")
    private Boolean isGlobal = false;
}
