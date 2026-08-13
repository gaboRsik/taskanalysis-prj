package com.taskanalysis.repository;

import com.taskanalysis.entity.SubtaskTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for SubtaskTag entity
 */
@Repository
public interface SubtaskTagRepository extends JpaRepository<SubtaskTag, Long> {

    /**
     * Find all global tags (visible to all users)
     * @return List of global tags
     */
    List<SubtaskTag> findByIsGlobalTrue();

    /**
     * Find all user-specific tags for a given user
     * @param userId User ID
     * @return List of user's tags
     */
    @Query("SELECT st FROM SubtaskTag st WHERE st.isGlobal = false AND st.user.id = :userId")
    List<SubtaskTag> findByUserIdAndIsGlobalFalse(@Param("userId") Long userId);

    /**
     * Find all tags visible to a user (global + user's own)
     * @param userId User ID
     * @return List of visible tags
     */
    @Query("SELECT st FROM SubtaskTag st WHERE st.isGlobal = true OR st.user.id = :userId")
    List<SubtaskTag> findAllVisibleToUser(@Param("userId") Long userId);

    /**
     * Check if a global tag with given name exists
     * @param name Tag name
     * @return true if exists
     */
    boolean existsByNameAndIsGlobalTrue(String name);

    /**
     * Check if a user already has a tag with given name
     * @param name Tag name
     * @param userId User ID
     * @return true if exists
     */
    @Query("SELECT CASE WHEN COUNT(st) > 0 THEN true ELSE false END FROM SubtaskTag st " +
           "WHERE st.name = :name AND st.isGlobal = false AND st.user.id = :userId")
    boolean existsByNameAndUserId(@Param("name") String name, @Param("userId") Long userId);

    /**
     * Find global tag by name
     * @param name Tag name
     * @return Optional of SubtaskTag
     */
    Optional<SubtaskTag> findByNameAndIsGlobalTrue(String name);

    /**
     * Find user-specific tag by name and user ID
     * @param name Tag name
     * @param userId User ID
     * @return Optional of SubtaskTag
     */
    @Query("SELECT st FROM SubtaskTag st WHERE st.name = :name AND st.isGlobal = false AND st.user.id = :userId")
    Optional<SubtaskTag> findByNameAndUserId(@Param("name") String name, @Param("userId") Long userId);

    /**
     * Find tags by IDs that are visible to a specific user
     * @param tagIds Tag IDs
     * @param userId User ID
     * @return List of visible tags
     */
    @Query("SELECT st FROM SubtaskTag st WHERE st.id IN :tagIds AND (st.isGlobal = true OR st.user.id = :userId)")
    List<SubtaskTag> findByIdsVisibleToUser(@Param("tagIds") List<Long> tagIds, @Param("userId") Long userId);

    /**
     * Count tags created by a user
     * @param userId User ID
     * @return Count of tags
     */
    @Query("SELECT COUNT(st) FROM SubtaskTag st WHERE st.createdBy.id = :userId")
    Long countByCreatedByUserId(@Param("userId") Long userId);

    /**
     * Find all tags used in subtasks of a specific task
     * @param taskId Task ID
     * @return List of tags
     */
    @Query("SELECT DISTINCT st FROM SubtaskTag st " +
           "JOIN st.subtasks s " +
           "WHERE s.task.id = :taskId")
    List<SubtaskTag> findTagsByTaskId(@Param("taskId") Long taskId);
}
