package com.taskanalysis.repository;

import com.taskanalysis.entity.Subtask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubtaskRepository extends JpaRepository<Subtask, Long> {

    List<Subtask> findByTaskId(Long taskId);

    Optional<Subtask> findByTaskIdAndSubtaskNumber(Long taskId, Integer subtaskNumber);

    /**
     * Batch-fetch tags for multiple subtasks in a single query, avoiding N+1
     * lazy-loading of the tags collection per subtask.
     * Each row is [subtaskId (Long), tag (SubtaskTag)].
     */
    @Query("SELECT s.id, t FROM Subtask s JOIN s.tags t WHERE s.id IN :subtaskIds")
    List<Object[]> findTagsForSubtaskIds(@Param("subtaskIds") List<Long> subtaskIds);

}
