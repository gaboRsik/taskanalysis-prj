package com.taskanalysis.repository;

import com.taskanalysis.entity.Subtask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubtaskRepository extends JpaRepository<Subtask, Long> {
    
    List<Subtask> findByTaskId(Long taskId);
    
    Optional<Subtask> findByTaskIdAndSubtaskNumber(Long taskId, Integer subtaskNumber);
    
}
