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

    // ===== Computed Metrics (Transient - not persisted) =====

    /**
     * Calculate proportional planned time for this subtask
     * Formula: (subtask plannedPoints / task totalPlannedPoints) * task plannedTotalTimeMinutes
     * @return Proportional planned time in minutes, or null if insufficient data
     */
    @Transient
    public Integer getProportionalPlannedTimeMinutes() {
        if (task == null || plannedPoints == null || task.getPlannedTotalTimeMinutes() == null) {
            return null;
        }
        
        Integer totalPlannedPoints = task.getTotalPlannedPoints();
        if (totalPlannedPoints == null || totalPlannedPoints == 0) {
            return null;
        }
        
        return (int) Math.round((plannedPoints.doubleValue() / totalPlannedPoints) * task.getPlannedTotalTimeMinutes());
    }

    /**
     * Calculate total actual time spent on this subtask
     * @return Total time in seconds
     */
    @Transient
    public Integer getTotalActualTimeSeconds() {
        if (timeEntries == null || timeEntries.isEmpty()) {
            return 0;
        }
        
        long total = timeEntries.stream()
                .mapToLong(entry -> entry.getDurationSeconds() != null ? entry.getDurationSeconds().longValue() : 0L)
                .sum();
        
        return (int) total;
    }

    /**
     * Calculate planned efficiency score (points per minute)
     * Formula: plannedPoints / proportionalPlannedTimeMinutes
     * @return Efficiency score or null if insufficient data
     */
    @Transient
    public Double getPlannedEfficiencyScore() {
        Integer proportionalTime = getProportionalPlannedTimeMinutes();
        if (proportionalTime == null || proportionalTime == 0 || plannedPoints == null || plannedPoints == 0) {
            return null;
        }
        
        return plannedPoints / proportionalTime.doubleValue();
    }

    /**
     * Calculate actual efficiency score (points per minute)
     * Formula: actualPoints / (totalTimeSeconds / 60)
     * @return Efficiency score or null if insufficient data
     */
    @Transient
    public Double getActualEfficiencyScore() {
        Integer totalTime = getTotalActualTimeSeconds();
        if (totalTime == null || totalTime == 0 || actualPoints == null || actualPoints == 0) {
            return null;
        }
        
        return actualPoints / (totalTime / 60.0);
    }

    /**
     * Calculate planned time per point (minutes)
     * Formula: proportionalPlannedTimeMinutes / plannedPoints
     * @return Minutes per point or null if insufficient data
     */
    @Transient
    public Double getPlannedTimePerPoint() {
        Integer proportionalTime = getProportionalPlannedTimeMinutes();
        if (proportionalTime == null || plannedPoints == null || plannedPoints == 0) {
            return null;
        }
        
        return proportionalTime / plannedPoints.doubleValue();
    }

    /**
     * Calculate actual time per point (minutes)
     * Formula: (totalTimeSeconds / 60) / actualPoints
     * @return Minutes per point or null if insufficient data
     */
    @Transient
    public Double getActualTimePerPoint() {
        Integer totalTime = getTotalActualTimeSeconds();
        if (totalTime == null || totalTime == 0 || actualPoints == null || actualPoints == 0) {
            return null;
        }
        
        return (totalTime / 60.0) / actualPoints;
    }

    /**
     * Calculate efficiency variance percentage
     * Formula: ((actualEfficiency - plannedEfficiency) / plannedEfficiency) * 100
     * Positive = better than planned, Negative = worse than planned
     * @return Variance percentage or null if insufficient data
     */
    @Transient
    public Double getEfficiencyVariancePercent() {
        Double planned = getPlannedEfficiencyScore();
        Double actual = getActualEfficiencyScore();
        
        if (planned == null || actual == null || planned == 0) {
            return null;
        }
        
        return ((actual - planned) / planned) * 100;
    }

    /**
     * Calculate time variance percentage
     * Formula: ((actualTime - plannedTime) / plannedTime) * 100
     * Positive = took longer, Negative = took less time
     * @return Variance percentage or null if insufficient data
     */
    @Transient
    public Double getTimeVariancePercent() {
        Integer proportionalTime = getProportionalPlannedTimeMinutes();
        Integer totalTime = getTotalActualTimeSeconds();
        
        if (proportionalTime == null || proportionalTime == 0 || totalTime == null) {
            return null;
        }
        
        double actualTimeMinutes = totalTime / 60.0;
        return ((actualTimeMinutes - proportionalTime) / proportionalTime) * 100;
    }

}
