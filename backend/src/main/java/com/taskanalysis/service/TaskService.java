package com.taskanalysis.service;

import com.taskanalysis.dto.subtask.SubtaskResponse;
import com.taskanalysis.dto.task.TaskRequest;
import com.taskanalysis.dto.task.TaskResponse;
import com.taskanalysis.entity.*;
import com.taskanalysis.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private SubtaskRepository subtaskRepository;

    @Autowired
    private TimeEntryRepository timeEntryRepository;

    @Transactional
    public TaskResponse createTask(Long userId, TaskRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Task task = new Task();
        task.setUser(user);
        task.setName(request.getName());
        task.setDescription(request.getDescription());
        task.setSubtaskCount(request.getSubtaskCount());

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            
            if (!category.getUser().getId().equals(userId)) {
                throw new RuntimeException("Access denied to category");
            }
            task.setCategory(category);
        }

        Task savedTask = taskRepository.save(task);

        // Create subtasks automatically
        List<Subtask> subtasks = new ArrayList<>();
        for (int i = 1; i <= request.getSubtaskCount(); i++) {
            Subtask subtask = new Subtask();
            subtask.setTask(savedTask);
            subtask.setSubtaskNumber(i);
            subtask.setStatus(Subtask.SubtaskStatus.NOT_STARTED);
            subtasks.add(subtask);
        }
        subtaskRepository.saveAll(subtasks);

        return mapToResponse(savedTask, subtasks);
    }

    public List<TaskResponse> getUserTasks(Long userId) {
        List<Task> tasks = taskRepository.findByUserId(userId);
        return tasks.stream()
                .map(task -> {
                    List<Subtask> subtasks = subtaskRepository.findByTaskId(task.getId());
                    return mapToResponse(task, subtasks);
                })
                .collect(Collectors.toList());
    }

    public TaskResponse getTaskById(Long userId, Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        if (!task.getUser().getId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }

        List<Subtask> subtasks = subtaskRepository.findByTaskId(taskId);
        return mapToResponse(task, subtasks);
    }

    @Transactional
    public TaskResponse updateTask(Long userId, Long taskId, TaskRequest request) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        if (!task.getUser().getId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }

        task.setName(request.getName());
        task.setDescription(request.getDescription());

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            
            if (!category.getUser().getId().equals(userId)) {
                throw new RuntimeException("Access denied to category");
            }
            task.setCategory(category);
        } else {
            task.setCategory(null);
        }

        Task updated = taskRepository.save(task);
        List<Subtask> subtasks = subtaskRepository.findByTaskId(taskId);
        return mapToResponse(updated, subtasks);
    }

    @Transactional
    public void deleteTask(Long userId, Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        if (!task.getUser().getId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }

        taskRepository.delete(task);
    }

    private TaskResponse mapToResponse(Task task, List<Subtask> subtasks) {
        TaskResponse response = new TaskResponse();
        response.setId(task.getId());
        response.setName(task.getName());
        response.setDescription(task.getDescription());
        response.setSubtaskCount(task.getSubtaskCount());
        response.setStatus(task.getStatus());
        response.setCreatedAt(task.getCreatedAt());
        response.setUpdatedAt(task.getUpdatedAt());

        if (task.getCategory() != null) {
            response.setCategoryId(task.getCategory().getId());
            response.setCategoryName(task.getCategory().getName());
        }

        List<SubtaskResponse> subtaskResponses = subtasks.stream()
                .map(this::mapSubtaskToResponse)
                .collect(Collectors.toList());
        response.setSubtasks(subtaskResponses);

        return response;
    }

    private SubtaskResponse mapSubtaskToResponse(Subtask subtask) {
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
