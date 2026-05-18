package com.taskanalysis.service;

import com.taskanalysis.entity.Subtask;
import com.taskanalysis.entity.Task;
import com.taskanalysis.entity.TimeEntry;
import com.taskanalysis.repository.SubtaskRepository;
import com.taskanalysis.repository.TaskRepository;
import com.taskanalysis.repository.TimeEntryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskStatusService {

    private final TaskRepository taskRepository;
    private final SubtaskRepository subtaskRepository;
    private final TimeEntryRepository timeEntryRepository;

    /**
     * Change task status with validation
     * 
     * @param taskId Task to update
     * @param userId User making the change
     * @param newStatus Target status
     * @return Updated task
     * @throws RuntimeException if validation fails
     */
    @Transactional
    public Task changeTaskStatus(Long taskId, Long userId, Task.TaskStatus newStatus) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        if (!task.getUser().getId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }

        // Subtasks are automatically loaded due to @Transactional
        // Trigger lazy loading by accessing the collection
        task.getSubtasks().size();

        // Validate status transition
        validateStatusTransition(task, newStatus);

        Task.TaskStatus oldStatus = task.getStatus();
        task.setStatus(newStatus);

        // If completing the task, cascade to subtasks
        if (newStatus == Task.TaskStatus.COMPLETED) {
            completeTaskAndSubtasks(task);
        }

        Task updatedTask = taskRepository.save(task);
        
        log.info("Task {} status changed from {} to {} by user {}", 
                taskId, oldStatus, newStatus, userId);

        return updatedTask;
    }

    /**
     * Validate status transition rules
     */
    private void validateStatusTransition(Task task, Task.TaskStatus newStatus) {
        Task.TaskStatus currentStatus = task.getStatus();

        // Rule: NOT_STARTED → COMPLETED is forbidden
        if (currentStatus == Task.TaskStatus.NOT_STARTED && 
            newStatus == Task.TaskStatus.COMPLETED) {
            throw new RuntimeException(
                "Cannot complete task that has not started. Start the task first.");
        }

        // Rule: Cannot start task without planned time
        if (currentStatus == Task.TaskStatus.NOT_STARTED && 
            newStatus == Task.TaskStatus.IN_PROGRESS &&
            task.getPlannedTotalTimeMinutes() == null) {
            throw new RuntimeException(
                "Cannot start task without planned total time. Please set planned time first.");
        }
    }

    /**
     * Complete task and all IN_PROGRESS subtasks
     */
    private void completeTaskAndSubtasks(Task task) {
        LocalDateTime completionTime = LocalDateTime.now();
        
        for (Subtask subtask : task.getSubtasks()) {
            if (subtask.getStatus() == Subtask.SubtaskStatus.IN_PROGRESS) {
                // Stop running timer for this subtask
                timeEntryRepository.findFirstBySubtaskIdAndEndTimeIsNull(subtask.getId())
                    .ifPresent(entry -> {
                        entry.setEndTime(completionTime);
                        entry.setDurationSeconds(
                            java.time.Duration.between(entry.getStartTime(), completionTime).getSeconds()
                        );
                        timeEntryRepository.save(entry);
                        log.info("Stopped timer for subtask {} at task completion", subtask.getId());
                    });
                
                // Mark subtask as completed
                subtask.setStatus(Subtask.SubtaskStatus.COMPLETED);
                subtaskRepository.save(subtask);
            }
        }
    }

    /**
     * Validate time constraint: Σ(subtask.totalTimeSeconds) ≤ task.planned_total_time_minutes * 60
     */
    public boolean validateTimeConstraint(Task task) {
        if (task.getPlannedTotalTimeMinutes() == null) {
            return true; // No constraint if no planned time
        }

        int plannedSeconds = task.getPlannedTotalTimeMinutes() * 60;
        int actualSeconds = task.getTotalActualTimeSeconds();

        return actualSeconds <= plannedSeconds;
    }

    /**
     * Check if task can be analyzed by AI
     */
    public boolean canAnalyzeWithAI(Task task) {
        // Must be COMPLETED
        if (task.getStatus() != Task.TaskStatus.COMPLETED) {
            return false;
        }

        // Must have time data
        if (task.getTotalActualTimeSeconds() == null || task.getTotalActualTimeSeconds() == 0) {
            return false;
        }

        // Must have points data
        if (task.getTotalActualPoints() == null || task.getTotalActualPoints() == 0) {
            return false;
        }

        return true;
    }
}
