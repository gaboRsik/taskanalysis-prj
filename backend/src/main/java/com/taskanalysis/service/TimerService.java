package com.taskanalysis.service;

import com.taskanalysis.dto.timer.TimerResponse;
import com.taskanalysis.entity.Subtask;
import com.taskanalysis.entity.Task;
import com.taskanalysis.entity.TimeEntry;
import com.taskanalysis.repository.SubtaskRepository;
import com.taskanalysis.repository.TimeEntryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TimerService {

    @Autowired
    private SubtaskRepository subtaskRepository;

    @Autowired
    private TimeEntryRepository timeEntryRepository;

    @Transactional
    public TimerResponse startTimer(Long userId, Long subtaskId) {
        Subtask subtask = subtaskRepository.findById(subtaskId)
                .orElseThrow(() -> new RuntimeException("Subtask not found"));

        if (!subtask.getTask().getUser().getId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }

        // Validate: Task must have planned total time to start timer
        Task task = subtask.getTask();
        if (task.getPlannedTotalTimeMinutes() == null) {
            throw new RuntimeException("Cannot start timer: Task must have planned total time set first");
        }

        // Stop any other running timer for this task
        stopAllTimersForTask(task.getId());

        // Check if there's already a running timer for this subtask
        Optional<TimeEntry> existingRunning = timeEntryRepository.findFirstBySubtaskIdAndEndTimeIsNull(subtaskId);
        if (existingRunning.isPresent()) {
            return mapToResponse(existingRunning.get(), subtask);
        }

        // Create new time entry
        TimeEntry timeEntry = new TimeEntry();
        timeEntry.setSubtask(subtask);
        timeEntry.setStartTime(LocalDateTime.now());

        TimeEntry saved = timeEntryRepository.save(timeEntry);

        // Update subtask and task status
        if (subtask.getStatus() == Subtask.SubtaskStatus.NOT_STARTED) {
            subtask.setStatus(Subtask.SubtaskStatus.IN_PROGRESS);
            subtaskRepository.save(subtask);
        }

        if (task.getStatus() == Task.TaskStatus.NOT_STARTED) {
            task.setStatus(Task.TaskStatus.IN_PROGRESS);
        }

        return mapToResponse(saved, subtask);
    }

    @Transactional
    public TimerResponse stopTimer(Long userId, Long subtaskId) {
        Subtask subtask = subtaskRepository.findById(subtaskId)
                .orElseThrow(() -> new RuntimeException("Subtask not found"));

        if (!subtask.getTask().getUser().getId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }

        TimeEntry timeEntry = timeEntryRepository.findFirstBySubtaskIdAndEndTimeIsNull(subtaskId)
                .orElseThrow(() -> new RuntimeException("No running timer found for this subtask"));

        LocalDateTime endTime = LocalDateTime.now();
        timeEntry.setEndTime(endTime);
        
        long durationSeconds = Duration.between(timeEntry.getStartTime(), endTime).getSeconds();
        timeEntry.setDurationSeconds(durationSeconds);

        TimeEntry saved = timeEntryRepository.save(timeEntry);
        
        // Check if time limit reached and auto-complete task if necessary
        Task task = subtask.getTask();
        boolean timeLimitReached = false;
        boolean taskAutoCompleted = false;
        
        if (task.getPlannedTotalTimeMinutes() != null) {
            // Calculate total time spent on task (refresh from DB to get latest)
            subtaskRepository.flush(); // Ensure latest data
            Task refreshedTask = task; // Already has updated relationships
            long totalTaskTimeSeconds = refreshedTask.getTotalActualTimeSeconds();
            long plannedTimeSeconds = task.getPlannedTotalTimeMinutes() * 60L;
            
            // Check if we've reached or exceeded the planned time
            if (totalTaskTimeSeconds >= plannedTimeSeconds) {
                timeLimitReached = true;
                
                // Auto-complete task if not already completed
                if (task.getStatus() != Task.TaskStatus.COMPLETED) {
                    task.setStatus(Task.TaskStatus.COMPLETED);
                    
                    // Auto-complete all subtasks as well
                    for (Subtask st : task.getSubtasks()) {
                        if (st.getStatus() != Subtask.SubtaskStatus.COMPLETED) {
                            st.setStatus(Subtask.SubtaskStatus.COMPLETED);
                        }
                    }
                    
                    // subtaskRepository will cascade save the task
                    taskAutoCompleted = true;
                }
            }
        }
        
        return mapToResponse(saved, subtask, timeLimitReached, taskAutoCompleted);
    }

    @Transactional(readOnly = true)
    public TimerResponse getActiveTimer(Long userId, Long taskId) {
        // Find any running timer for this task
        List<Subtask> subtasks = subtaskRepository.findByTaskId(taskId);
        
        for (Subtask subtask : subtasks) {
            if (!subtask.getTask().getUser().getId().equals(userId)) {
                throw new RuntimeException("Access denied");
            }

            Optional<TimeEntry> running = timeEntryRepository.findFirstBySubtaskIdAndEndTimeIsNull(subtask.getId());
            if (running.isPresent()) {
                return mapToResponse(running.get(), subtask);
            }
        }

        return null;
    }

    @Transactional
    public TimerResponse stopCurrentTimer(Long userId) {
        // Find any running timer for this user
        List<TimeEntry> runningEntries = timeEntryRepository.findByEndTimeIsNull();
        
        for (TimeEntry timeEntry : runningEntries) {
            Subtask subtask = timeEntry.getSubtask();
            if (subtask.getTask().getUser().getId().equals(userId)) {
                LocalDateTime endTime = LocalDateTime.now();
                timeEntry.setEndTime(endTime);
                long durationSeconds = Duration.between(timeEntry.getStartTime(), endTime).getSeconds();
                timeEntry.setDurationSeconds(durationSeconds);
                TimeEntry saved = timeEntryRepository.save(timeEntry);
                
                // Check time limit
                Task task = subtask.getTask();
                boolean timeLimitReached = false;
                boolean taskAutoCompleted = false;
                
                if (task.getPlannedTotalTimeMinutes() != null) {
                    subtaskRepository.flush();
                    long totalTaskTimeSeconds = task.getTotalActualTimeSeconds();
                    long plannedTimeSeconds = task.getPlannedTotalTimeMinutes() * 60L;
                    
                    if (totalTaskTimeSeconds >= plannedTimeSeconds) {
                        timeLimitReached = true;
                        if (task.getStatus() != Task.TaskStatus.COMPLETED) {
                            task.setStatus(Task.TaskStatus.COMPLETED);
                            
                            // Auto-complete all subtasks as well
                            for (Subtask st : task.getSubtasks()) {
                                if (st.getStatus() != Subtask.SubtaskStatus.COMPLETED) {
                                    st.setStatus(Subtask.SubtaskStatus.COMPLETED);
                                }
                            }
                            
                            taskAutoCompleted = true;
                        }
                    }
                }
                
                return mapToResponse(saved, subtask, timeLimitReached, taskAutoCompleted);
            }
        }
        
        throw new RuntimeException("No running timer found");
    }

    @Transactional(readOnly = true)
    public TimerResponse getActiveTimerForUser(Long userId) {
        // Find any running timer for this user
        List<TimeEntry> runningEntries = timeEntryRepository.findByEndTimeIsNull();
        
        for (TimeEntry timeEntry : runningEntries) {
            Subtask subtask = timeEntry.getSubtask();
            if (subtask.getTask().getUser().getId().equals(userId)) {
                return mapToResponse(timeEntry, subtask);
            }
        }
        
        return null;
    }

    // Helper method - runs in caller's transaction
    private void stopAllTimersForTask(Long taskId) {
        List<Subtask> subtasks = subtaskRepository.findByTaskId(taskId);
        LocalDateTime now = LocalDateTime.now();

        for (Subtask subtask : subtasks) {
            Optional<TimeEntry> running = timeEntryRepository.findFirstBySubtaskIdAndEndTimeIsNull(subtask.getId());
            if (running.isPresent()) {
                TimeEntry entry = running.get();
                entry.setEndTime(now);
                long durationSeconds = Duration.between(entry.getStartTime(), now).getSeconds();
                entry.setDurationSeconds(durationSeconds);
                timeEntryRepository.save(entry);
            }
        }
    }

    private TimerResponse mapToResponse(TimeEntry timeEntry, Subtask subtask) {
        return mapToResponse(timeEntry, subtask, false, false);
    }
    
    private TimerResponse mapToResponse(TimeEntry timeEntry, Subtask subtask, boolean timeLimitReached, boolean taskAutoCompleted) {
        boolean isRunning = timeEntry.getEndTime() == null;
        Long duration = timeEntry.getDurationSeconds();

        if (isRunning && timeEntry.getStartTime() != null) {
            duration = Duration.between(timeEntry.getStartTime(), LocalDateTime.now()).getSeconds();
        }

        Task task = subtask.getTask();
        String taskTitle = task.getName();
        String subtaskTitle = "Subtask #" + subtask.getSubtaskNumber();
        
        // Calculate total task time and planned time
        Long totalTaskTimeSeconds = (long) task.getTotalActualTimeSeconds();
        Long plannedTimeSeconds = task.getPlannedTotalTimeMinutes() != null 
                ? task.getPlannedTotalTimeMinutes() * 60L 
                : null;

        TimerResponse response = new TimerResponse();
        response.setTimeEntryId(timeEntry.getId());
        response.setSubtaskId(subtask.getId());
        response.setSubtaskNumber(subtask.getSubtaskNumber());
        response.setTaskTitle(taskTitle);
        response.setSubtaskTitle(subtaskTitle);
        response.setStartTime(timeEntry.getStartTime());
        response.setEndTime(timeEntry.getEndTime());
        response.setDurationSeconds(duration);
        response.setRunning(isRunning);
        response.setTimeLimitReached(timeLimitReached);
        response.setTaskAutoCompleted(taskAutoCompleted);
        response.setTotalTaskTimeSeconds(totalTaskTimeSeconds);
        response.setPlannedTimeSeconds(plannedTimeSeconds);
        
        return response;
    }

}
