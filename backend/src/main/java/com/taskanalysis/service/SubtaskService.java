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
