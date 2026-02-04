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

    public SubtaskResponse getSubtaskById(Long userId, Long subtaskId) {
        Subtask subtask = subtaskRepository.findById(subtaskId)
                .orElseThrow(() -> new RuntimeException("Subtask not found"));

        if (!subtask.getTask().getUser().getId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }

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
        return mapToResponse(updated);
    }

    private SubtaskResponse mapToResponse(Subtask subtask) {
        List<TimeEntry> timeEntries = timeEntryRepository.findBySubtaskId(subtask.getId());
        long totalSeconds = timeEntries.stream()
                .filter(entry -> entry.getDurationSeconds() != null)
                .mapToLong(TimeEntry::getDurationSeconds)
                .sum();

        return new SubtaskResponse(
                subtask.getId(),
                subtask.getTask().getId(),
                subtask.getSubtaskNumber(),
                subtask.getPlannedPoints(),
                subtask.getActualPoints(),
                subtask.getStatus(),
                totalSeconds,
                subtask.getCreatedAt(),
                subtask.getUpdatedAt()
        );
    }

}
