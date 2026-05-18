package com.taskanalysis.scheduler;

import com.taskanalysis.entity.Subtask;
import com.taskanalysis.entity.Task;
import com.taskanalysis.entity.TimeEntry;
import com.taskanalysis.repository.SubtaskRepository;
import com.taskanalysis.repository.TaskRepository;
import com.taskanalysis.repository.TimeEntryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class TaskCompletionScheduler {

    private final TaskRepository taskRepository;
    private final SubtaskRepository subtaskRepository;
    private final TimeEntryRepository timeEntryRepository;

    /**
     * Check every minute for tasks that have exceeded their planned time
     * and automatically complete them
     */
    @Scheduled(fixedRate = 60000) // Every 60 seconds
    @Transactional
    public void checkAndCompleteExpiredTasks() {
        log.debug("Checking for tasks with expired planned time...");

        List<Task> inProgressTasks = taskRepository.findByStatus(Task.TaskStatus.IN_PROGRESS);

        for (Task task : inProgressTasks) {
            if (task.getPlannedTotalTimeMinutes() != null && hasExceededPlannedTime(task)) {
                autoCompleteTask(task);
            }
        }
    }

    /**
     * Check if task has exceeded its planned time
     */
    private boolean hasExceededPlannedTime(Task task) {
        if (task.getPlannedTotalTimeMinutes() == null) {
            return false;
        }

        // Calculate total actual time from subtasks
        List<Subtask> subtasks = subtaskRepository.findByTaskId(task.getId());
        
        long totalActualSeconds = 0;
        for (Subtask subtask : subtasks) {
            List<TimeEntry> entries = timeEntryRepository.findBySubtaskId(subtask.getId());
            for (TimeEntry entry : entries) {
                if (entry.getDurationSeconds() != null) {
                    totalActualSeconds += entry.getDurationSeconds();
                }
                // Include currently running timers
                else if (entry.getStartTime() != null && entry.getEndTime() == null) {
                    long runningSeconds = java.time.Duration.between(
                        entry.getStartTime(), LocalDateTime.now()).getSeconds();
                    totalActualSeconds += runningSeconds;
                }
            }
        }

        long plannedSeconds = (long) task.getPlannedTotalTimeMinutes() * 60;
        
        return totalActualSeconds >= plannedSeconds;
    }

    /**
     * Automatically complete task when planned time expires
     */
    private void autoCompleteTask(Task task) {
        log.info("Auto-completing task {} - planned time expired", task.getId());

        LocalDateTime completionTime = LocalDateTime.now();
        
        // Load subtasks
        List<Subtask> subtasks = subtaskRepository.findByTaskId(task.getId());
        
        // Stop all running timers and complete IN_PROGRESS subtasks
        for (Subtask subtask : subtasks) {
            if (subtask.getStatus() == Subtask.SubtaskStatus.IN_PROGRESS) {
                // Stop running timer
                timeEntryRepository.findFirstBySubtaskIdAndEndTimeIsNull(subtask.getId())
                    .ifPresent(entry -> {
                        entry.setEndTime(completionTime);
                        entry.setDurationSeconds(
                            java.time.Duration.between(entry.getStartTime(), completionTime).getSeconds()
                        );
                        timeEntryRepository.save(entry);
                        log.info("Stopped timer for subtask {} (auto-completion)", subtask.getId());
                    });
                
                // Mark subtask as completed
                subtask.setStatus(Subtask.SubtaskStatus.COMPLETED);
                subtaskRepository.save(subtask);
            }
        }

        // Mark task as completed
        task.setStatus(Task.TaskStatus.COMPLETED);
        taskRepository.save(task);

        log.info("Task {} auto-completed successfully", task.getId());
        
        // TODO: Send notification to user (frontend will poll or use WebSocket)
    }
}
