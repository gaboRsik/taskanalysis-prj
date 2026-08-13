package com.taskanalysis.service;

import com.taskanalysis.dto.CreateTagRequest;
import com.taskanalysis.dto.SubtaskTagDTO;
import com.taskanalysis.entity.Role;
import com.taskanalysis.entity.SubtaskTag;
import com.taskanalysis.entity.User;
import com.taskanalysis.repository.SubtaskTagRepository;
import com.taskanalysis.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing subtask tags (global and user-specific)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SubtaskTagService {

    private final SubtaskTagRepository tagRepository;
    private final UserRepository userRepository;

    /**
     * Get all tags visible to user (global + user's own)
     *
     * @param userId User ID
     * @return List of visible tags
     */
    @Transactional(readOnly = true)
    public List<SubtaskTagDTO> getAllTagsForUser(Long userId) {
        log.info("Getting all tags for userId={}", userId);

        // Get global tags
        List<SubtaskTag> globalTags = tagRepository.findByIsGlobalTrue();

        // Get user's own tags
        List<SubtaskTag> userTags = tagRepository.findByUserIdAndIsGlobalFalse(userId);

        // Combine both lists
        List<SubtaskTag> allTags = new ArrayList<>();
        allTags.addAll(globalTags);
        allTags.addAll(userTags);

        log.info("Found {} global tags and {} user-specific tags for userId={}",
                globalTags.size(), userTags.size(), userId);

        return allTags.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get all GLOBAL tags only (for admin UI)
     *
     * @return List of global tags
     */
    @Transactional(readOnly = true)
    public List<SubtaskTagDTO> getAllGlobalTags() {
        log.info("Getting all global tags");
        return tagRepository.findByIsGlobalTrue().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Create new tag
     * - Admin can create global or user-specific tags
     * - User can only create user-specific tags
     *
     * @param request Tag creation request
     * @param userId User creating the tag
     * @return Created tag DTO
     */
    @Transactional
    public SubtaskTagDTO createTag(CreateTagRequest request, Long userId) {
        log.info("Creating tag: name={}, isGlobal={}, userId={}", 
                request.getName(), request.getIsGlobal(), userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean isAdmin = user.getRole() == Role.ADMIN;
        boolean createAsGlobal = request.getIsGlobal() != null && request.getIsGlobal() && isAdmin;

        // Authorization check
        if (request.getIsGlobal() != null && request.getIsGlobal() && !isAdmin) {
            log.warn("User userId={} attempted to create global tag without admin rights", userId);
            throw new SecurityException("Only admins can create global tags");
        }

        // Check if tag already exists
        if (createAsGlobal) {
            // Global tag name uniqueness
            if (tagRepository.existsByNameAndIsGlobalTrue(request.getName())) {
                log.warn("Global tag with name '{}' already exists", request.getName());
                throw new IllegalArgumentException("Global tag with this name already exists");
            }
        } else {
            // User-specific tag uniqueness
            if (tagRepository.existsByNameAndUserId(request.getName(), userId)) {
                log.warn("User userId={} already has a tag with name '{}'", userId, request.getName());
                throw new IllegalArgumentException("You already have a tag with this name");
            }
        }

        // Create tag
        SubtaskTag tag = SubtaskTag.builder()
                .name(request.getName().trim())
                .color(request.getColor().toLowerCase())
                .isGlobal(createAsGlobal)
                .user(createAsGlobal ? null : user)  // Global tag → user = null
                .createdBy(user)
                .build();

        SubtaskTag saved = tagRepository.save(tag);

        log.info("Created {} tag: id={}, name={}, userId={}",
                createAsGlobal ? "GLOBAL" : "USER-SPECIFIC",
                saved.getId(), saved.getName(), userId);

        return toDTO(saved);
    }

    /**
     * Update tag
     * - Only admin can update global tags
     * - Only owner can update user-specific tags
     *
     * @param tagId Tag ID to update
     * @param request Update request
     * @param userId User requesting update
     * @return Updated tag DTO
     */
    @Transactional
    public SubtaskTagDTO updateTag(Long tagId, CreateTagRequest request, Long userId) {
        log.info("Updating tag: tagId={}, userId={}", tagId, userId);

        SubtaskTag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new RuntimeException("Tag not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Authorization check
        if (tag.getIsGlobal()) {
            // Global tag → csak admin módosíthatja
            if (user.getRole() != Role.ADMIN) {
                log.warn("User userId={} attempted to update global tag tagId={} without admin rights",
                        userId, tagId);
                throw new SecurityException("Only admins can update global tags");
            }
        } else {
            // User-specific tag → csak a tulajdonos módosíthatja
            if (tag.getUser() == null || !tag.getUser().getId().equals(userId)) {
                log.warn("User userId={} attempted to update another user's tag tagId={}",
                        userId, tagId);
                throw new SecurityException("You can only update your own tags");
            }
        }

        // Check name uniqueness (if name changed)
        if (!tag.getName().equals(request.getName())) {
            if (tag.getIsGlobal()) {
                if (tagRepository.existsByNameAndIsGlobalTrue(request.getName())) {
                    throw new IllegalArgumentException("Another global tag with this name already exists");
                }
            } else {
                if (tagRepository.existsByNameAndUserId(request.getName(), userId)) {
                    throw new IllegalArgumentException("You already have another tag with this name");
                }
            }
        }

        // Update fields
        tag.setName(request.getName().trim());
        tag.setColor(request.getColor().toLowerCase());

        SubtaskTag updated = tagRepository.save(tag);

        log.info("Updated tag: tagId={}, name={}, userId={}", tagId, updated.getName(), userId);

        return toDTO(updated);
    }

    /**
     * Delete tag
     * - Only admin can delete global tags
     * - Only owner can delete user-specific tags
     *
     * @param tagId Tag ID to delete
     * @param userId User requesting deletion
     */
    @Transactional
    public void deleteTag(Long tagId, Long userId) {
        log.info("Deleting tag: tagId={}, userId={}", tagId, userId);

        SubtaskTag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new RuntimeException("Tag not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Authorization check
        if (tag.getIsGlobal()) {
            if (user.getRole() != Role.ADMIN) {
                log.warn("User userId={} attempted to delete global tag tagId={} without admin rights",
                        userId, tagId);
                throw new SecurityException("Only admins can delete global tags");
            }
        } else {
            if (tag.getUser() == null || !tag.getUser().getId().equals(userId)) {
                log.warn("User userId={} attempted to delete another user's tag tagId={}",
                        userId, tagId);
                throw new SecurityException("You can only delete your own tags");
            }
        }

        // Delete tag (cascades to subtask_tag_mapping)
        tagRepository.delete(tag);

        log.info("Deleted tag: tagId={}, name={}, userId={}", tagId, tag.getName(), userId);
    }

    /**
     * Get tag by ID (with visibility check)
     *
     * @param tagId Tag ID
     * @param userId User requesting tag
     * @return Tag DTO
     */
    @Transactional(readOnly = true)
    public SubtaskTagDTO getTagById(Long tagId, Long userId) {
        SubtaskTag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new RuntimeException("Tag not found"));

        // Visibility check
        if (!tag.isVisibleToUser(userId)) {
            throw new SecurityException("You don't have access to this tag");
        }

        return toDTO(tag);
    }

    /**
     * Get tags by IDs (with visibility check)
     *
     * @param tagIds List of tag IDs
     * @param userId User requesting tags
     * @return List of tags visible to user
     */
    @Transactional(readOnly = true)
    public List<SubtaskTagDTO> getTagsByIds(List<Long> tagIds, Long userId) {
        List<SubtaskTag> tags = tagRepository.findByIdsVisibleToUser(tagIds, userId);
        return tags.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Convert SubtaskTag entity to DTO
     *
     * @param tag Entity
     * @return DTO
     */
    private SubtaskTagDTO toDTO(SubtaskTag tag) {
        return SubtaskTagDTO.builder()
                .id(tag.getId())
                .name(tag.getName())
                .color(tag.getColor())
                .isGlobal(tag.getIsGlobal())
                .userId(tag.getUser() != null ? tag.getUser().getId() : null)
                .createdById(tag.getCreatedBy() != null ? tag.getCreatedBy().getId() : null)
                .createdAt(tag.getCreatedAt())
                .usageCount(tag.getSubtasks() != null ? tag.getSubtasks().size() : 0)
                .build();
    }
}
