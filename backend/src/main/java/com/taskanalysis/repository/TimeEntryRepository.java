package com.taskanalysis.repository;

import com.taskanalysis.entity.TimeEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TimeEntryRepository extends JpaRepository<TimeEntry, Long> {
    
    List<TimeEntry> findBySubtaskId(Long subtaskId);
    
    Optional<TimeEntry> findFirstBySubtaskIdAndEndTimeIsNull(Long subtaskId);
    
}
