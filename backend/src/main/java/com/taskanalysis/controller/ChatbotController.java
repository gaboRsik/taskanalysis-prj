package com.taskanalysis.controller;

import com.taskanalysis.dto.ChatbotAnalysisResponse;
import com.taskanalysis.entity.User;
import com.taskanalysis.repository.UserRepository;
import com.taskanalysis.security.CurrentUser;
import com.taskanalysis.service.ChatbotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chatbot")
@RequiredArgsConstructor
@Slf4j
public class ChatbotController {

    private final ChatbotService chatbotService;
    private final CurrentUser currentUser;
    private final UserRepository userRepository;

    /**
     * Analyze task performance
     * 
     * POST /api/chatbot/analyze/{taskId}
     */
    @PostMapping("/analyze/{taskId}")
    public ResponseEntity<ChatbotAnalysisResponse> analyzeTask(@PathVariable Long taskId) {
        log.info("Received chatbot analysis request for taskId={}", taskId);
        
        // Get current user ID
        Long userId = getCurrentUserId();
        
        // Analyze task
        ChatbotAnalysisResponse response = chatbotService.analyzeTaskPerformance(taskId, userId);
        
        return ResponseEntity.ok(response);
    }

    private Long getCurrentUserId() {
        String email = currentUser.getEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }
}
