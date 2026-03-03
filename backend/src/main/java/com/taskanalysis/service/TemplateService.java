package com.taskanalysis.service;

import com.taskanalysis.dto.template.TemplateRequest;
import com.taskanalysis.dto.template.TemplateResponse;
import com.taskanalysis.dto.template.TemplateSubtaskDTO;
import com.taskanalysis.entity.*;
import com.taskanalysis.exception.BusinessException;
import com.taskanalysis.exception.ResourceNotFoundException;
import com.taskanalysis.repository.CategoryRepository;
import com.taskanalysis.repository.TaskTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing task templates
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TemplateService {

    private final TaskTemplateRepository templateRepository;
    private final CategoryRepository categoryRepository;

    /**
     * Get all templates for the current user
     */
    @Transactional(readOnly = true)
    public List<TemplateResponse> getAllTemplates(User user) {
        log.info("Fetching all templates for user: {}", user.getEmail());
        List<TaskTemplate> templates = templateRepository.findByUserOrderByCreatedAtDesc(user);
        return templates.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get a specific template by ID
     */
    @Transactional(readOnly = true)
    public TemplateResponse getTemplateById(Long id, User user) {
        log.info("Fetching template {} for user: {}", id, user.getEmail());
        TaskTemplate template = templateRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found with id: " + id));
        return convertToResponse(template);
    }

    /**
     * Create a new template
     */
    @Transactional
    public TemplateResponse createTemplate(TemplateRequest request, User user) {
        log.info("Creating new template '{}' for user: {}", request.getName(), user.getEmail());

        // Validate category (required)
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));
        
        // Ensure category belongs to the user
        if (!category.getUser().getId().equals(user.getId())) {
            throw new BusinessException("Category does not belong to the current user");
        }

        // Check if template name already exists for this user in this category
        if (templateRepository.existsByUserAndCategoryAndName(user, category, request.getName())) {
            throw new BusinessException("A template with name '" + request.getName() + "' already exists in this category");
        }

        // Create template entity
        TaskTemplate template = new TaskTemplate();
        template.setUser(user);
        template.setCategory(category);
        template.setName(request.getName());
        template.setDescription(request.getDescription());
        template.setSubtaskCount(request.getSubtaskCount());
        template.setTaskCount(request.getTaskCount());

        // Create template subtasks
        if (request.getTemplateSubtasks() != null && !request.getTemplateSubtasks().isEmpty()) {
            List<TemplateSubtask> templateSubtasks = new ArrayList<>();
            
            for (TemplateSubtaskDTO dto : request.getTemplateSubtasks()) {
                TemplateSubtask templateSubtask = new TemplateSubtask();
                templateSubtask.setTemplate(template);
                templateSubtask.setSubtaskNumber(dto.getSubtaskNumber());
                templateSubtask.setPlannedPoints(dto.getPlannedPoints());
                templateSubtasks.add(templateSubtask);
            }
            
            template.setTemplateSubtasks(templateSubtasks);
        }

        TaskTemplate savedTemplate = templateRepository.save(template);
        log.info("Template created successfully with id: {}", savedTemplate.getId());
        
        return convertToResponse(savedTemplate);
    }

    /**
     * Update an existing template
     */
    @Transactional
    public TemplateResponse updateTemplate(Long id, TemplateRequest request, User user) {
        log.info("Updating template {} for user: {}", id, user.getEmail());

        TaskTemplate template = templateRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found with id: " + id));

        // Validate category (required)
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));
        
        if (!category.getUser().getId().equals(user.getId())) {
            throw new BusinessException("Category does not belong to the current user");
        }

        // Check if new name conflicts with another template in the same category
        if (!template.getName().equals(request.getName()) && 
            templateRepository.existsByUserAndCategoryAndName(user, category, request.getName())) {
            throw new BusinessException("A template with name '" + request.getName() + "' already exists in this category");
        }

        // Update template fields
        template.setName(request.getName());
        template.setDescription(request.getDescription());
        template.setCategory(category);
        template.setSubtaskCount(request.getSubtaskCount());
        template.setTaskCount(request.getTaskCount());

        // Update template subtasks
        template.getTemplateSubtasks().clear();
        
        if (request.getTemplateSubtasks() != null && !request.getTemplateSubtasks().isEmpty()) {
            for (TemplateSubtaskDTO dto : request.getTemplateSubtasks()) {
                TemplateSubtask templateSubtask = new TemplateSubtask();
                templateSubtask.setTemplate(template);
                templateSubtask.setSubtaskNumber(dto.getSubtaskNumber());
                templateSubtask.setPlannedPoints(dto.getPlannedPoints());
                template.getTemplateSubtasks().add(templateSubtask);
            }
        }

        TaskTemplate updatedTemplate = templateRepository.save(template);
        log.info("Template {} updated successfully", id);
        
        return convertToResponse(updatedTemplate);
    }

    /**
     * Delete a template
     */
    @Transactional
    public void deleteTemplate(Long id, User user) {
        log.info("Deleting template {} for user: {}", id, user.getEmail());

        TaskTemplate template = templateRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found with id: " + id));

        templateRepository.delete(template);
        log.info("Template {} deleted successfully", id);
    }

    /**
     * Convert TaskTemplate entity to TemplateResponse DTO
     */
    private TemplateResponse convertToResponse(TaskTemplate template) {
        List<TemplateSubtaskDTO> subtaskDTOs = template.getTemplateSubtasks().stream()
                .map(ts -> new TemplateSubtaskDTO(ts.getSubtaskNumber(), ts.getPlannedPoints()))
                .sorted((a, b) -> a.getSubtaskNumber().compareTo(b.getSubtaskNumber()))
                .collect(Collectors.toList());

        return TemplateResponse.builder()
                .id(template.getId())
                .name(template.getName())
                .description(template.getDescription())
                .categoryId(template.getCategory().getId())
                .categoryName(template.getCategory().getName())
                .subtaskCount(template.getSubtaskCount())
                .taskCount(template.getTaskCount())
                .templateSubtasks(subtaskDTOs)
                .createdAt(template.getCreatedAt())
                .updatedAt(template.getUpdatedAt())
                .build();
    }
}
