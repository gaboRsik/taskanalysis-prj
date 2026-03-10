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
@Table(name = "tasks", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_category_id", columnList = "category_id"),
    @Index(name = "idx_status", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "subtask_count", nullable = false)
    private Integer subtaskCount = 1;

    @Column(name = "planned_total_time_minutes")
    private Integer plannedTotalTimeMinutes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TaskStatus status = TaskStatus.NOT_STARTED;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Subtask> subtasks = new ArrayList<>();

    public enum TaskStatus {
        NOT_STARTED,
        IN_PROGRESS,
        COMPLETED
    }

    // ========================================
    // COMPUTED METRICS (Transient - not persisted)
    // ========================================

    /**
     * Calculate total actual time spent on all subtasks in seconds
     */
    @Transient
    public Integer getTotalActualTimeSeconds() {
        if (subtasks == null || subtasks.isEmpty()) {
            return 0;
        }
        long total = subtasks.stream()
                .mapToLong(subtask -> {
                    if (subtask.getTimeEntries() == null) return 0L;
                    return subtask.getTimeEntries().stream()
                            .mapToLong(entry -> entry.getDurationSeconds() != null ? entry.getDurationSeconds().longValue() : 0L)
                            .sum();
                })
                .sum();
        return (int) total;
    }

    /**
     * Calculate total planned points across all subtasks
     */
    @Transient
    public Integer getTotalPlannedPoints() {
        if (subtasks == null || subtasks.isEmpty()) {
            return 0;
        }
        return subtasks.stream()
                .filter(s -> s.getPlannedPoints() != null)
                .mapToInt(Subtask::getPlannedPoints)
                .sum();
    }

    /**
     * Calculate total actual points across all subtasks
     */
    @Transient
    public Integer getTotalActualPoints() {
        if (subtasks == null || subtasks.isEmpty()) {
            return 0;
        }
        return subtasks.stream()
                .filter(s -> s.getActualPoints() != null)
                .mapToInt(Subtask::getActualPoints)
                .sum();
    }

    /**
     * Planned Efficiency Score = Planned Points / Planned Time (minutes)
     * Higher = better planned efficiency
     */
    @Transient
    public Double getPlannedEfficiencyScore() {
        if (plannedTotalTimeMinutes == null || plannedTotalTimeMinutes == 0) {
            return null;
        }
        Integer totalPlanned = getTotalPlannedPoints();
        if (totalPlanned == 0) {
            return null;
        }
        return totalPlanned / (double) plannedTotalTimeMinutes;
    }

    /**
     * Actual Efficiency Score = Actual Points / Actual Time (minutes)
     * Higher = better actual efficiency
     */
    @Transient
    public Double getActualEfficiencyScore() {
        Integer actualTime = getTotalActualTimeSeconds();
        if (actualTime == null || actualTime == 0) {
            return null;
        }
        Integer totalActual = getTotalActualPoints();
        if (totalActual == 0) {
            return null;
        }
        double minutes = actualTime / 60.0;
        return totalActual / minutes;
    }

    /**
     * Planned Time per Point = Planned Time (minutes) / Planned Points
     * Lower = more efficient planning
     */
    @Transient
    public Double getPlannedTimePerPoint() {
        if (plannedTotalTimeMinutes == null || plannedTotalTimeMinutes == 0) {
            return null;
        }
        Integer totalPlanned = getTotalPlannedPoints();
        if (totalPlanned == 0) {
            return null;
        }
        return (double) plannedTotalTimeMinutes / totalPlanned;
    }

    /**
     * Actual Time per Point = Actual Time (minutes) / Actual Points
     * Lower = more efficient execution
     */
    @Transient
    public Double getActualTimePerPoint() {
        Integer actualTime = getTotalActualTimeSeconds();
        if (actualTime == null || actualTime == 0) {
            return null;
        }
        Integer totalActual = getTotalActualPoints();
        if (totalActual == 0) {
            return null;
        }
        double minutes = actualTime / 60.0;
        return minutes / totalActual;
    }

    /**
     * Efficiency Variance = (Actual Efficiency - Planned Efficiency) / Planned Efficiency * 100
     * Positive = better than planned, Negative = worse than planned
     */
    @Transient
    public Double getEfficiencyVariancePercent() {
        Double planned = getPlannedEfficiencyScore();
        Double actual = getActualEfficiencyScore();
        if (planned == null || actual == null) {
            return null;
        }
        return ((actual - planned) / planned) * 100;
    }

}
