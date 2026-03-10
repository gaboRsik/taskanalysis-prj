package com.taskanalysis.service;

import com.taskanalysis.dto.subtask.SubtaskRequest;
import com.taskanalysis.dto.subtask.SubtaskResponse;
import com.taskanalysis.entity.Subtask;
import com.taskanalysis.entity.TimeEntry;
import com.taskanalysis.repository.SubtaskRepository;
import com.taskanalysis.repository.TimeEntryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SubtaskService {

    @Autowired
    private SubtaskRepository subtaskRepository;

    @Autowired
    private TimeEntryRepository timeEntryRepository;

    @Transactional(readOnly = true)
    public SubtaskResponse getSubtaskById(Long userId, Long subtaskId) {
        Subtask subtask = subtaskRepository.findById(subtaskId)
                .orElseThrow(() -> new RuntimeException("Subtask not found"));

        if (!subtask.getTask().getUser().getId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }

        // Initialize lazy collections for metrics calculation
        subtask.getTask().getSubtasks().size(); // Force initialization of task's subtasks
        subtask.getTimeEntries().size(); // Force initialization of subtask's time entries
        
        return mapToResponse(subtask);
    }

    @Transactional
    public SubtaskResponse updateSubtask(Long userId, Long subtaskId, SubtaskRequest request) {
        Subtask subtask = subtaskRepository.findById(subtaskId)
                .orElseThrow(() -> new RuntimeException("Subtask not found"));

        if (!subtask.getTask().getUser().getId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }

        if (request.getPlannedPoints() != null) {
            subtask.setPlannedPoints(request.getPlannedPoints());
        }
        if (request.getActualPoints() != null) {
            subtask.setActualPoints(request.getActualPoints());
        }

        // Validate that actual points don't exceed planned points
        if (subtask.getActualPoints() != null && subtask.getPlannedPoints() != null) {
            if (subtask.getActualPoints() > subtask.getPlannedPoints()) {
                throw new RuntimeException("Actual points (" + subtask.getActualPoints() + 
                    ") cannot exceed planned points (" + subtask.getPlannedPoints() + ")");
            }
        }

        // Validate that actual points > 0 only if there is time spent on the subtask
        if (subtask.getActualPoints() != null && subtask.getActualPoints() > 0) {
            List<TimeEntry> timeEntries = timeEntryRepository.findBySubtaskId(subtaskId);
            long totalSeconds = timeEntries.stream()
                    .filter(entry -> entry.getDurationSeconds() != null)
                    .mapToLong(TimeEntry::getDurationSeconds)
                    .sum();
            
            if (totalSeconds == 0) {
                throw new RuntimeException("Cannot assign actual points to a subtask with no time spent. Please track time before adding points.");
            }
        }

        Subtask updated = subtaskRepository.save(subtask);
        
        // Initialize lazy collections for metrics calculation
        // This ensures the task's subtasks collection and subtask's time entries are loaded
        updated.getTask().getSubtasks().size(); // Force initialization of task's subtasks
        updated.getTimeEntries().size(); // Force initialization of subtask's time entries
        
        return mapToResponse(updated);
    }

    private SubtaskResponse mapToResponse(Subtask subtask) {
        List<TimeEntry> timeEntries = timeEntryRepository.findBySubtaskId(subtask.getId());
        long totalSeconds = timeEntries.stream()
                .filter(entry -> entry.getDurationSeconds() != null)
                .mapToLong(TimeEntry::getDurationSeconds)
                .sum();

        SubtaskResponse response = new SubtaskResponse();
        response.setId(subtask.getId());
        response.setTaskId(subtask.getTask().getId());
        response.setSubtaskNumber(subtask.getSubtaskNumber());
        response.setPlannedPoints(subtask.getPlannedPoints());
        response.setActualPoints(subtask.getActualPoints());
        response.setStatus(subtask.getStatus());
        response.setTotalTimeSeconds(totalSeconds);
        response.setCreatedAt(subtask.getCreatedAt());
        response.setUpdatedAt(subtask.getUpdatedAt());

        // Populate computed metrics from @Transient methods
        response.setProportionalPlannedTimeMinutes(subtask.getProportionalPlannedTimeMinutes());
        response.setPlannedEfficiencyScore(subtask.getPlannedEfficiencyScore());
        response.setActualEfficiencyScore(subtask.getActualEfficiencyScore());
        response.setPlannedTimePerPoint(subtask.getPlannedTimePerPoint());
        response.setActualTimePerPoint(subtask.getActualTimePerPoint());
        response.setEfficiencyVariancePercent(subtask.getEfficiencyVariancePercent());
        response.setTimeVariancePercent(subtask.getTimeVariancePercent());

        return response;
    }

}
