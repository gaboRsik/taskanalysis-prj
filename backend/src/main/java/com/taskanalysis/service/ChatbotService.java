package com.taskanalysis.service;

import com.taskanalysis.dto.ChatbotAnalysisResponse;
import com.taskanalysis.entity.Subtask;
import com.taskanalysis.entity.Task;
import dev.langchain4j.model.chat.ChatLanguageModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatbotService {

    private final ChatLanguageModel chatLanguageModel;
    private final TaskService taskService;
    private final TaskStatusService taskStatusService;

    /**
     * Analyze task performance using AI
     * 
     * @param taskId Task to analyze
     * @param userId User requesting analysis (security)
     * @return AI-generated analysis
     */
    @Transactional(readOnly = true)
    public ChatbotAnalysisResponse analyzeTaskPerformance(Long taskId, Long userId) {
        log.info("Analyzing task performance for taskId={}, userId={}", taskId, userId);
        
        // 1. Fetch task with all relationships loaded
        Task task = taskService.getTaskEntityById(taskId, userId);
        
        // 2. Validate task can be analyzed
        if (!taskStatusService.canAnalyzeWithAI(task)) {
            throw new RuntimeException(
                "AI analysis requires a COMPLETED task with time and points data. " +
                "Status: " + task.getStatus() + 
                ", Time: " + task.getTotalActualTimeSeconds() + "s" +
                ", Points: " + task.getTotalActualPoints()
            );
        }
        
        // 3. Build prompt with task data
        String prompt = buildAnalysisPrompt(task);
        
        // 4. Call LLM
        String aiResponse = callLLM(prompt);
        
        // 5. Build response
        return ChatbotAnalysisResponse.builder()
                .analysis(aiResponse)
                .timestamp(LocalDateTime.now())
                .model("llama-3.3-70b-versatile")
                .taskId(taskId)
                .taskName(task.getName())
                .build();
    }

    /**
     * Build prompt with task performance data
     */
    private String buildAnalysisPrompt(Task task) {
        // Calculate metrics
        int totalPlannedMinutes = task.getPlannedTotalTimeMinutes() != null 
                ? task.getPlannedTotalTimeMinutes() : 0;
        int totalActualMinutes = task.getTotalActualTimeSeconds() / 60;
        int totalPlannedPoints = task.getTotalPlannedPoints();
        int totalActualPoints = task.getTotalActualPoints();
        
        // Calculate efficiency
        double timeEfficiency = totalPlannedMinutes > 0 
                ? (double) totalPlannedMinutes / totalActualMinutes * 100 
                : 0;
        double pointsEfficiency = totalPlannedPoints > 0 
                ? (double) totalActualPoints / totalPlannedPoints * 100 
                : 0;
        
        // Subtask breakdown
        List<Subtask> subtasks = task.getSubtasks();
        StringBuilder subtaskDetails = new StringBuilder();
        for (Subtask subtask : subtasks) {
            long subtaskMinutes = calculateSubtaskMinutes(subtask);
            subtaskDetails.append(String.format(
                "  - Részfeladat #%d: %d perc, %d/%d pont%n",
                subtask.getSubtaskNumber(),
                subtaskMinutes,
                subtask.getActualPoints() != null ? subtask.getActualPoints() : 0,
                subtask.getPlannedPoints() != null ? subtask.getPlannedPoints() : 0
            ));
        }
        
        // Build prompt
        return String.format("""
            Te egy task management asszisztens vagy, aki segít a felhasználóknak 
            értékelni a teljesítményüket és javaslatokat ad a javításra.
            
            Értékeld a következő feladat teljesítményét MAGYARUL, röviden (max 200 szó):
            
            📋 Feladat adatok:
            - Név: "%s"
            - Kategória: %s
            - Státusz: %s
            - Részfeladatok száma: %d
            
            ⏱️ Idő teljesítmény:
            - Tervezett: %d perc
            - Tényleges: %d perc
            - Hatékonyság: %.1f%%
            
            🎯 Pont teljesítmény:
            - Tervezett: %d pont
            - Elért: %d pont
            - Teljesítés: %.1f%%
            
            📊 Részfeladatok:
            %s
            
            Adj egy személyes, motiváló értékelést ami tartalmazza:
            1. 🎉 Pozitív visszajelzés (mit csinált jól)
            2. 📊 Számszerű összefoglaló (idő, pontok)
            3. 💡 1-2 konkrét javaslat a következő task-ra
            
            Használj emotikonokat és barátságos hangnemet! 😊
            """,
            task.getName(),
            task.getCategory() != null ? task.getCategory().getName() : "Nincs kategória",
            task.getStatus(),
            subtasks.size(),
            totalPlannedMinutes,
            totalActualMinutes,
            timeEfficiency,
            totalPlannedPoints,
            totalActualPoints,
            pointsEfficiency,
            subtaskDetails.toString()
        );
    }

    /**
     * Call LLM with prompt
     */
    private String callLLM(String prompt) {
        try {
            log.debug("Sending prompt to LLM: {}", prompt);
            String response = chatLanguageModel.generate(prompt);
            log.debug("Received response: {}", response);
            return response;
        } catch (Exception e) {
            log.error("Error calling LLM", e);
            return "Sajnos nem sikerült az értékelést generálni. Próbáld újra később! 😔";
        }
    }

    /**
     * Calculate subtask total time in minutes
     */
    private long calculateSubtaskMinutes(Subtask subtask) {
        return subtask.getTimeEntries().stream()
                .filter(entry -> entry.getDurationSeconds() != null)
                .mapToLong(entry -> entry.getDurationSeconds().longValue())
                .sum() / 60;
    }
}
