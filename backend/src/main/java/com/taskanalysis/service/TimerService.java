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

        // Stop any other running timer for this task
        stopAllTimersForTask(subtask.getTask().getId());

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

        Task task = subtask.getTask();
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
        return mapToResponse(saved, subtask);
    }

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
        boolean isRunning = timeEntry.getEndTime() == null;
        Long duration = timeEntry.getDurationSeconds();

        if (isRunning && timeEntry.getStartTime() != null) {
            duration = Duration.between(timeEntry.getStartTime(), LocalDateTime.now()).getSeconds();
        }

        return new TimerResponse(
                timeEntry.getId(),
                subtask.getId(),
                subtask.getSubtaskNumber(),
                timeEntry.getStartTime(),
                timeEntry.getEndTime(),
                duration,
                isRunning
        );
    }

}
