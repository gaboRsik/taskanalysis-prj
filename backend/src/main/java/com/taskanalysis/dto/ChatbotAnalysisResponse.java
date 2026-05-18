package com.taskanalysis.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatbotAnalysisResponse {
    private String analysis;           // AI generált szöveg
    private LocalDateTime timestamp;   // Mikor generálódott
    private Integer tokensUsed;        // Token használat (költség tracking)
    private String model;              // Melyik model-t használtuk
    
    // Metadata (opcionális, később analytics-hez)
    private Long taskId;
    private String taskName;
}
