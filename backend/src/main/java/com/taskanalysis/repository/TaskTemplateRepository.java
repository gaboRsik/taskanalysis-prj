package com.taskanalysis.repository;

import com.taskanalysis.entity.Category;
import com.taskanalysis.entity.TaskTemplate;
import com.taskanalysis.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for TaskTemplate entity
 */
@Repository
public interface TaskTemplateRepository extends JpaRepository<TaskTemplate, Long> {

    /**
     * Find all templates for a specific user
     */
    List<TaskTemplate> findByUserOrderByCreatedAtDesc(User user);

    /**
     * Find a template by ID and user (ensures user can only access their own templates)
     */
    Optional<TaskTemplate> findByIdAndUser(Long id, User user);

    /**
     * Check if a template name already exists for a user in a specific category
     */
    boolean existsByUserAndCategoryAndName(User user, Category category, String name);

    /**
     * Delete a template by ID and user (ensures user can only delete their own templates)
     */
    void deleteByIdAndUser(Long id, User user);
}
