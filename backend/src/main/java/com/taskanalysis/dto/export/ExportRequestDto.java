package com.taskanalysis.dto.export;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExportRequestDto {
    
    @NotNull(message = "Export format is required")
    private ExportFormat format;
    
    @NotNull(message = "Delivery method is required")
    private DeliveryMethod delivery;
}
