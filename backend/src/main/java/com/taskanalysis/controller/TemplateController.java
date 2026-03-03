package com.taskanalysis.controller;

import com.taskanalysis.dto.task.TaskResponse;
import com.taskanalysis.dto.template.TemplateRequest;
import com.taskanalysis.dto.template.TemplateResponse;
import com.taskanalysis.entity.User;
import com.taskanalysis.repository.UserRepository;
import com.taskanalysis.security.CurrentUser;
import com.taskanalysis.service.TaskService;
import com.taskanalysis.service.TemplateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Task Template management
 */
@RestController
@RequestMapping("/templates")
@RequiredArgsConstructor
public class TemplateController {

    private final TemplateService templateService;
    private final TaskService taskService;
    private final CurrentUser currentUser;
    private final UserRepository userRepository;

    /**
     * Get all templates for the current user
     * GET /api/templates
     */
    @GetMapping
    public ResponseEntity<List<TemplateResponse>> getAllTemplates() {
        User user = getCurrentUser();
        List<TemplateResponse> templates = templateService.getAllTemplates(user);
        return ResponseEntity.ok(templates);
    }

    /**
     * Get a specific template by ID
     * GET /api/templates/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<TemplateResponse> getTemplateById(@PathVariable Long id) {
        User user = getCurrentUser();
        TemplateResponse template = templateService.getTemplateById(id, user);
        return ResponseEntity.ok(template);
    }

    /**
     * Create a new template
     * POST /api/templates
     */
    @PostMapping
    public ResponseEntity<TemplateResponse> createTemplate(@Valid @RequestBody TemplateRequest request) {
        User user = getCurrentUser();
        TemplateResponse template = templateService.createTemplate(request, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(template);
    }

    /**
     * Update an existing template
     * PUT /api/templates/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<TemplateResponse> updateTemplate(
            @PathVariable Long id,
            @Valid @RequestBody TemplateRequest request) {
        User user = getCurrentUser();
        TemplateResponse template = templateService.updateTemplate(id, request, user);
        return ResponseEntity.ok(template);
    }

    /**
     * Delete a template
     * DELETE /api/templates/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTemplate(@PathVariable Long id) {
        User user = getCurrentUser();
        templateService.deleteTemplate(id, user);
        return ResponseEntity.noContent().build();
    }

    /**
     * Create multiple tasks from a template
     * POST /api/templates/{id}/create-tasks
     */
    @PostMapping("/{id}/create-tasks")
    public ResponseEntity<List<TaskResponse>> createTasksFromTemplate(@PathVariable Long id) {
        Long userId = getCurrentUser().getId();
        List<TaskResponse> tasks = taskService.createTasksFromTemplate(userId, id);
        return ResponseEntity.status(HttpStatus.CREATED).body(tasks);
    }

    /**
     * Get current authenticated user
     */
    private User getCurrentUser() {
        String email = currentUser.getEmail();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
