package com.taskanalysis.dto.export;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExportResponseDto {
    
    private boolean success;
    private String message;
    private DeliveryMethod deliveryMethod;
    private String fileName;
    
    public static ExportResponseDto success(DeliveryMethod delivery, String fileName, String message) {
        return new ExportResponseDto(true, message, delivery, fileName);
    }
    
    public static ExportResponseDto error(String message) {
        return new ExportResponseDto(false, message, null, null);
    }
}
