package com.taskanalysis.service;

import com.taskanalysis.dto.template.TemplateRequest;
import com.taskanalysis.dto.template.TemplateResponse;
import com.taskanalysis.dto.template.TemplateSubtaskDTO;
import com.taskanalysis.entity.*;
import com.taskanalysis.exception.BusinessException;
import com.taskanalysis.exception.ResourceNotFoundException;
import com.taskanalysis.repository.CategoryRepository;
import com.taskanalysis.repository.TaskTemplateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TemplateService
 */
@ExtendWith(MockitoExtension.class)
class TemplateServiceTest {

    @Mock
    private TaskTemplateRepository templateRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private TemplateService templateService;

    private User testUser;
    private Category testCategory;
    private Category otherCategory;
    private TaskTemplate testTemplate;
    private TemplateRequest testRequest;

    @BeforeEach
    void setUp() {
        // Create test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setName("Test User");

        // Create test categories
        testCategory = new Category();
        testCategory.setId(1L);
        testCategory.setName("Python Development");
        testCategory.setUser(testUser);

        otherCategory = new Category();
        otherCategory.setId(2L);
        otherCategory.setName("Java Development");
        otherCategory.setUser(testUser);

        // Create test template
        testTemplate = new TaskTemplate();
        testTemplate.setId(1L);
        testTemplate.setUser(testUser);
        testTemplate.setCategory(testCategory);
        testTemplate.setName("API Endpoint");
        testTemplate.setDescription("Create new REST API endpoint");
        testTemplate.setSubtaskCount(3);
        testTemplate.setCreatedAt(LocalDateTime.now());
        testTemplate.setUpdatedAt(LocalDateTime.now());

        // Create template subtasks
        List<TemplateSubtask> subtasks = new ArrayList<>();
        TemplateSubtask subtask1 = new TemplateSubtask();
        subtask1.setId(1L);
        subtask1.setTemplate(testTemplate);
        subtask1.setSubtaskNumber(1);
        subtask1.setPlannedPoints(5);
        subtasks.add(subtask1);

        TemplateSubtask subtask2 = new TemplateSubtask();
        subtask2.setId(2L);
        subtask2.setTemplate(testTemplate);
        subtask2.setSubtaskNumber(2);
        subtask2.setPlannedPoints(3);
        subtasks.add(subtask2);

        testTemplate.setTemplateSubtasks(subtasks);

        // Create test request
        testRequest = new TemplateRequest();
        testRequest.setName("API Endpoint");
        testRequest.setDescription("Create new REST API endpoint");
        testRequest.setCategoryId(1L);
        testRequest.setSubtaskCount(3);
        
        List<TemplateSubtaskDTO> subtaskDTOs = Arrays.asList(
            new TemplateSubtaskDTO(1, 5),
            new TemplateSubtaskDTO(2, 3)
        );
        testRequest.setTemplateSubtasks(subtaskDTOs);
    }

    // ========== getAllTemplates Tests ==========

    @Test
    @DisplayName("Should return all templates for user")
    void testGetAllTemplates() {
        // Arrange
        List<TaskTemplate> templates = Arrays.asList(testTemplate);
        when(templateRepository.findByUserOrderByCreatedAtDesc(testUser)).thenReturn(templates);

        // Act
        List<TemplateResponse> result = templateService.getAllTemplates(testUser);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("API Endpoint", result.get(0).getName());
        assertEquals(1L, result.get(0).getCategoryId());
        assertEquals("Python Development", result.get(0).getCategoryName());
        verify(templateRepository).findByUserOrderByCreatedAtDesc(testUser);
    }

    @Test
    @DisplayName("Should return empty list when user has no templates")
    void testGetAllTemplatesEmpty() {
        // Arrange
        when(templateRepository.findByUserOrderByCreatedAtDesc(testUser)).thenReturn(new ArrayList<>());

        // Act
        List<TemplateResponse> result = templateService.getAllTemplates(testUser);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(templateRepository).findByUserOrderByCreatedAtDesc(testUser);
    }

    // ========== getTemplateById Tests ==========

    @Test
    @DisplayName("Should return template by id")
    void testGetTemplateById() {
        // Arrange
        when(templateRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(testTemplate));

        // Act
        TemplateResponse result = templateService.getTemplateById(1L, testUser);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("API Endpoint", result.getName());
        assertEquals(1L, result.getCategoryId());
        assertEquals("Python Development", result.getCategoryName());
        assertEquals(2, result.getTemplateSubtasks().size());
        verify(templateRepository).findByIdAndUser(1L, testUser);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when template not found")
    void testGetTemplateByIdNotFound() {
        // Arrange
        when(templateRepository.findByIdAndUser(999L, testUser)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> templateService.getTemplateById(999L, testUser)
        );
        
        assertTrue(exception.getMessage().contains("Template not found with id: 999"));
        verify(templateRepository).findByIdAndUser(999L, testUser);
    }

    // ========== createTemplate Tests ==========

    @Test
    @DisplayName("Should create template successfully")
    void testCreateTemplate() {
        // Arrange
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(templateRepository.existsByUserAndCategoryAndName(testUser, testCategory, "API Endpoint")).thenReturn(false);
        when(templateRepository.save(any(TaskTemplate.class))).thenReturn(testTemplate);

        // Act
        TemplateResponse result = templateService.createTemplate(testRequest, testUser);

        // Assert
        assertNotNull(result);
        assertEquals("API Endpoint", result.getName());
        assertEquals(1L, result.getCategoryId());
        assertEquals("Python Development", result.getCategoryName());
        verify(categoryRepository).findById(1L);
        verify(templateRepository).existsByUserAndCategoryAndName(testUser, testCategory, "API Endpoint");
        verify(templateRepository).save(any(TaskTemplate.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when category not found")
    void testCreateTemplateCategoryNotFound() {
        // Arrange
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());
        testRequest.setCategoryId(999L);

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> templateService.createTemplate(testRequest, testUser)
        );
        
        assertTrue(exception.getMessage().contains("Category not found with id: 999"));
        verify(categoryRepository).findById(999L);
        verify(templateRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw BusinessException when category does not belong to user")
    void testCreateTemplateCategoryNotBelongsToUser() {
        // Arrange
        User otherUser = new User();
        otherUser.setId(2L);
        otherUser.setEmail("other@example.com");
        
        Category otherUserCategory = new Category();
        otherUserCategory.setId(3L);
        otherUserCategory.setName("Other Category");
        otherUserCategory.setUser(otherUser);
        
        testRequest.setCategoryId(3L);
        when(categoryRepository.findById(3L)).thenReturn(Optional.of(otherUserCategory));

        // Act & Assert
        BusinessException exception = assertThrows(
            BusinessException.class,
            () -> templateService.createTemplate(testRequest, testUser)
        );
        
        assertTrue(exception.getMessage().contains("Category does not belong to the current user"));
        verify(categoryRepository).findById(3L);
        verify(templateRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw BusinessException when template name already exists in category")
    void testCreateTemplateDuplicateNameInCategory() {
        // Arrange
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(templateRepository.existsByUserAndCategoryAndName(testUser, testCategory, "API Endpoint")).thenReturn(true);

        // Act & Assert
        BusinessException exception = assertThrows(
            BusinessException.class,
            () -> templateService.createTemplate(testRequest, testUser)
        );
        
        assertTrue(exception.getMessage().contains("A template with name 'API Endpoint' already exists in this category"));
        verify(categoryRepository).findById(1L);
        verify(templateRepository).existsByUserAndCategoryAndName(testUser, testCategory, "API Endpoint");
        verify(templateRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should allow same template name in different category")
    void testCreateTemplateSameNameDifferentCategory() {
        // Arrange
        testRequest.setCategoryId(2L); // Different category
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(otherCategory));
        when(templateRepository.existsByUserAndCategoryAndName(testUser, otherCategory, "API Endpoint")).thenReturn(false);
        
        TaskTemplate newTemplate = new TaskTemplate();
        newTemplate.setId(2L);
        newTemplate.setUser(testUser);
        newTemplate.setCategory(otherCategory);
        newTemplate.setName("API Endpoint");
        newTemplate.setDescription("Create new REST API endpoint");
        newTemplate.setSubtaskCount(3);
        newTemplate.setTemplateSubtasks(new ArrayList<>());
        newTemplate.setCreatedAt(LocalDateTime.now());
        newTemplate.setUpdatedAt(LocalDateTime.now());
        
        when(templateRepository.save(any(TaskTemplate.class))).thenReturn(newTemplate);

        // Act
        TemplateResponse result = templateService.createTemplate(testRequest, testUser);

        // Assert
        assertNotNull(result);
        assertEquals("API Endpoint", result.getName());
        assertEquals(2L, result.getCategoryId());
        assertEquals("Java Development", result.getCategoryName());
        verify(categoryRepository).findById(2L);
        verify(templateRepository).existsByUserAndCategoryAndName(testUser, otherCategory, "API Endpoint");
        verify(templateRepository).save(any(TaskTemplate.class));
    }

    // ========== updateTemplate Tests ==========

    @Test
    @DisplayName("Should update template successfully")
    void testUpdateTemplate() {
        // Arrange
        when(templateRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(testTemplate));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        // Note: existsByUserAndCategoryAndName is not stubbed because name doesn't change
        when(templateRepository.save(any(TaskTemplate.class))).thenReturn(testTemplate);

        // Act
        TemplateResponse result = templateService.updateTemplate(1L, testRequest, testUser);

        // Assert
        assertNotNull(result);
        assertEquals("API Endpoint", result.getName());
        verify(templateRepository).findByIdAndUser(1L, testUser);
        verify(categoryRepository).findById(1L);
        verify(templateRepository).save(any(TaskTemplate.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when updating non-existing template")
    void testUpdateTemplateNotFound() {
        // Arrange
        when(templateRepository.findByIdAndUser(999L, testUser)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> templateService.updateTemplate(999L, testRequest, testUser)
        );
        
        assertTrue(exception.getMessage().contains("Template not found with id: 999"));
        verify(templateRepository).findByIdAndUser(999L, testUser);
        verify(templateRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw BusinessException when updating template with name conflict in category")
    void testUpdateTemplateNameConflict() {
        // Arrange
        testRequest.setName("Conflicting Name");
        when(templateRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(testTemplate));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(templateRepository.existsByUserAndCategoryAndName(testUser, testCategory, "Conflicting Name")).thenReturn(true);

        // Act & Assert
        BusinessException exception = assertThrows(
            BusinessException.class,
            () -> templateService.updateTemplate(1L, testRequest, testUser)
        );
        
        assertTrue(exception.getMessage().contains("A template with name 'Conflicting Name' already exists in this category"));
        verify(templateRepository).findByIdAndUser(1L, testUser);
        verify(categoryRepository).findById(1L);
        verify(templateRepository).existsByUserAndCategoryAndName(testUser, testCategory, "Conflicting Name");
        verify(templateRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw BusinessException when updating with category not belonging to user")
    void testUpdateTemplateCategoryNotBelongsToUser() {
        // Arrange
        User otherUser = new User();
        otherUser.setId(2L);
        
        Category otherUserCategory = new Category();
        otherUserCategory.setId(3L);
        otherUserCategory.setUser(otherUser);
        
        testRequest.setCategoryId(3L);
        when(templateRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(testTemplate));
        when(categoryRepository.findById(3L)).thenReturn(Optional.of(otherUserCategory));

        // Act & Assert
        BusinessException exception = assertThrows(
            BusinessException.class,
            () -> templateService.updateTemplate(1L, testRequest, testUser)
        );
        
        assertTrue(exception.getMessage().contains("Category does not belong to the current user"));
        verify(templateRepository).findByIdAndUser(1L, testUser);
        verify(categoryRepository).findById(3L);
        verify(templateRepository, never()).save(any());
    }

    // ========== deleteTemplate Tests ==========

    @Test
    @DisplayName("Should delete template successfully")
    void testDeleteTemplate() {
        // Arrange
        when(templateRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(testTemplate));
        doNothing().when(templateRepository).delete(testTemplate);

        // Act
        templateService.deleteTemplate(1L, testUser);

        // Assert
        verify(templateRepository).findByIdAndUser(1L, testUser);
        verify(templateRepository).delete(testTemplate);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when deleting non-existing template")
    void testDeleteTemplateNotFound() {
        // Arrange
        when(templateRepository.findByIdAndUser(999L, testUser)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> templateService.deleteTemplate(999L, testUser)
        );
        
        assertTrue(exception.getMessage().contains("Template not found with id: 999"));
        verify(templateRepository).findByIdAndUser(999L, testUser);
        verify(templateRepository, never()).delete(any());
    }
}
