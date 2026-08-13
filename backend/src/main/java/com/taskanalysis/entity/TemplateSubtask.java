package com.taskanalysis.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Template Subtask Entity
 * Represents a predefined subtask within a task template
 */
@Entity
@Table(name = "template_subtasks", indexes = {
    @Index(name = "idx_template_id", columnList = "template_id")
}, uniqueConstraints = {
    @UniqueConstraint(name = "unique_template_subtask", columnNames = {"template_id", "subtask_number"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TemplateSubtask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private TaskTemplate template;

    @Column(name = "subtask_number", nullable = false)
    private Integer subtaskNumber;

    @Column(name = "planned_points")
    private Integer plannedPoints;

    @ManyToMany
    @JoinTable(
        name = "template_subtask_tag_mapping",
        joinColumns = @JoinColumn(name = "template_subtask_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<SubtaskTag> tags = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Helper methods for managing tags
    public void addTag(SubtaskTag tag) {
        tags.add(tag);
        tag.getTemplateSubtasks().add(this);
    }

    public void removeTag(SubtaskTag tag) {
        tags.remove(tag);
        tag.getTemplateSubtasks().remove(this);
    }

    public void clearTags() {
        tags.forEach(tag -> tag.getTemplateSubtasks().remove(this));
        tags.clear();
    }
}
