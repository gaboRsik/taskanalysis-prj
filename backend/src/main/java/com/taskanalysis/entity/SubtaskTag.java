package com.taskanalysis.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Subtask tag entity - supports both global (admin) and user-specific tags
 */
@Entity
@Table(name = "subtask_tags", indexes = {
        @Index(name = "idx_subtask_tags_global", columnList = "is_global"),
        @Index(name = "idx_subtask_tags_user", columnList = "user_id"),
        @Index(name = "idx_subtask_tags_name", columnList = "name")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubtaskTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Tag name (e.g., "Frontend", "Backend", "Database")
     */
    @Column(nullable = false, length = 50)
    private String name;

    /**
     * Color in HEX format (e.g., "#667eea")
     */
    @Column(nullable = false, length = 7)
    @Builder.Default
    private String color = "#667eea";

    /**
     * Is this a global tag (visible to all users)?
     * - TRUE: Global tag (created by admin, visible to everyone)
     * - FALSE: User-specific tag (only visible to owner)
     */
    @Column(name = "is_global", nullable = false)
    @Builder.Default
    private Boolean isGlobal = false;

    /**
     * Owner of the tag (NULL for global tags)
     * - NULL: Global tag (created by admin)
     * - NOT NULL: User-specific tag (owned by this user)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * Who created this tag (admin or user)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private User createdBy;

    /**
     * When was this tag created
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Subtasks that use this tag
     */
    @ManyToMany(mappedBy = "tags", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Subtask> subtasks = new HashSet<>();

    /**
     * Template subtasks that use this tag
     */
    @ManyToMany(mappedBy = "tags", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<TemplateSubtask> templateSubtasks = new HashSet<>();

    /**
     * Check if this tag is visible to a specific user
     *
     * @param userId User ID to check visibility for
     * @return true if visible, false otherwise
     */
    public boolean isVisibleToUser(Long userId) {
        if (isGlobal) {
            return true;  // Global tags are visible to everyone
        }
        return user != null && user.getId().equals(userId);  // User-specific tags
    }

    /**
     * Check if this tag can be edited by a specific user
     *
     * @param userId User ID to check edit permission for
     * @param userRole User's role
     * @return true if editable, false otherwise
     */
    public boolean isEditableBy(Long userId, Role userRole) {
        if (isGlobal) {
            // Global tags can only be edited by admins
            return userRole == Role.ADMIN;
        } else {
            // User-specific tags can only be edited by their owner
            return user != null && user.getId().equals(userId);
        }
    }
}
