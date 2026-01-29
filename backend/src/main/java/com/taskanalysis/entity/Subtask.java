package com.taskanalysis.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "subtasks", indexes = {
    @Index(name = "idx_task_id", columnList = "task_id"),
    @Index(name = "idx_status", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Subtask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @Column(name = "subtask_number", nullable = false)
    private Integer subtaskNumber;

    @Column(name = "planned_points")
    private Integer plannedPoints;

    @Column(name = "actual_points")
    private Integer actualPoints;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private SubtaskStatus status = SubtaskStatus.NOT_STARTED;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "subtask", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TimeEntry> timeEntries = new ArrayList<>();

    public enum SubtaskStatus {
        NOT_STARTED,
        IN_PROGRESS,
        COMPLETED
    }

}
