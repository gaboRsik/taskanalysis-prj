package com.taskanalysis.service;

import com.taskanalysis.dto.subtask.SubtaskResponse;
import com.taskanalysis.dto.task.TaskRequest;
import com.taskanalysis.dto.task.TaskUpdateRequest;
import com.taskanalysis.dto.task.TaskResponse;
import com.taskanalysis.entity.*;
import com.taskanalysis.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Autowired
    private TaskTemplateRepository taskTemplateRepository;

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

    @Transactional(readOnly = true)
    public List<TaskResponse> getUserTasks(Long userId) {
        List<Task> tasks = taskRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return tasks.stream()
                .map(task -> {
                    List<Subtask> subtasks = subtaskRepository.findByTaskId(task.getId());
                    return mapToResponse(task, subtasks);
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TaskResponse getTaskById(Long userId, Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        if (!task.getUser().getId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }

        List<Subtask> subtasks = subtaskRepository.findByTaskId(taskId);
        return mapToResponse(task, subtasks);
    }

    /**
     * Get Task entity for export (with loaded relationships)
     */
    @Transactional(readOnly = true)
    public Task getTaskEntityById(Long taskId, Long userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        if (!task.getUser().getId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }

        // Ensure subtasks and time entries are loaded
        task.getSubtasks().size();
        task.getSubtasks().forEach(subtask -> subtask.getTimeEntries().size());
        
        return task;
    }

    @Transactional
    public TaskResponse updateTask(Long userId, Long taskId, TaskUpdateRequest request) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        if (!task.getUser().getId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }

        if (request.getName() != null) {
            task.setName(request.getName());
        }
        
        if (request.getDescription() != null) {
            task.setDescription(request.getDescription());
        }

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            
            if (!category.getUser().getId().equals(userId)) {
                throw new RuntimeException("Access denied to category");
            }
            task.setCategory(category);
        }
        // Ne változtassuk meg a kategóriát, ha nincs megadva categoryId

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

        // Calculate total planned and actual points
        Integer totalPlannedPoints = subtasks.stream()
                .map(Subtask::getPlannedPoints)
                .filter(points -> points != null)
                .reduce(0, Integer::sum);
        
        Integer totalActualPoints = subtasks.stream()
                .map(Subtask::getActualPoints)
                .filter(points -> points != null)
                .reduce(0, Integer::sum);
        
        response.setTotalPlannedPoints(totalPlannedPoints);
        response.setTotalActualPoints(totalActualPoints);

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

    /**
     * Create a task from a template
     * @param userId User ID
     * @param templateId Template ID
     * @return Created task response
    /**
     * Create multiple tasks from a template
     * @param userId User ID
     * @param templateId Template ID
     * @return List of created tasks
     */
    @Transactional
    public List<TaskResponse> createTasksFromTemplate(Long userId, Long templateId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        TaskTemplate template = taskTemplateRepository.findById(templateId)
                .orElseThrow(() -> new RuntimeException("Template not found"));

        // Ensure template belongs to the user
        if (!template.getUser().getId().equals(userId)) {
            throw new RuntimeException("Access denied to template");
        }

        List<TaskResponse> createdTasks = new ArrayList<>();

        // Create multiple tasks based on taskCount
        for (int taskNumber = 1; taskNumber <= template.getTaskCount(); taskNumber++) {
            // Create task from template
            Task task = new Task();
            task.setUser(user);
            task.setName(template.getName() + " #" + taskNumber);
            task.setDescription(template.getDescription());
            task.setSubtaskCount(template.getSubtaskCount());
            task.setCategory(template.getCategory());

            Task savedTask = taskRepository.save(task);

            // Create subtasks from template subtasks
            List<Subtask> subtasks = new ArrayList<>();
            
            if (template.getTemplateSubtasks() != null && !template.getTemplateSubtasks().isEmpty()) {
                // Create subtasks based on template subtasks with planned points
                for (TemplateSubtask templateSubtask : template.getTemplateSubtasks()) {
                    Subtask subtask = new Subtask();
                    subtask.setTask(savedTask);
                    subtask.setSubtaskNumber(templateSubtask.getSubtaskNumber());
                    subtask.setPlannedPoints(templateSubtask.getPlannedPoints());
                    subtask.setStatus(Subtask.SubtaskStatus.NOT_STARTED);
                    subtasks.add(subtask);
                }
            } else {
                // Create default subtasks if no template subtasks defined
                for (int i = 1; i <= template.getSubtaskCount(); i++) {
                    Subtask subtask = new Subtask();
                    subtask.setTask(savedTask);
                    subtask.setSubtaskNumber(i);
                    subtask.setStatus(Subtask.SubtaskStatus.NOT_STARTED);
                    subtasks.add(subtask);
                }
            }
            
            subtaskRepository.saveAll(subtasks);
            createdTasks.add(mapToResponse(savedTask, subtasks));
        }

        return createdTasks;
    }

}

