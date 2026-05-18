package com.taskanalysis.dto;

import lombok.Data;

@Data
public class ChatbotAnalysisRequest {
    // Opcionális: később conversation history-hoz
    private String conversationId;
    private String userMessage; // pl. "Adj több tippet!"
}
