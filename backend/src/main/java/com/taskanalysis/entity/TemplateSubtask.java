package com.taskanalysis.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

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

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
