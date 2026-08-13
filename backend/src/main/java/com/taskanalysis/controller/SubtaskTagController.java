package com.taskanalysis.controller;

import com.taskanalysis.dto.CreateTagRequest;
import com.taskanalysis.dto.SubtaskTagDTO;
import com.taskanalysis.entity.Role;
import com.taskanalysis.entity.User;
import com.taskanalysis.repository.UserRepository;
import com.taskanalysis.security.CurrentUser;
import com.taskanalysis.service.SubtaskTagService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for managing subtask tags
 */
@RestController
@RequestMapping("/subtask-tags")
@RequiredArgsConstructor
@Slf4j
public class SubtaskTagController {

    private final SubtaskTagService tagService;
    private final CurrentUser currentUser;
    private final UserRepository userRepository;

    /**
     * Get all tags visible to current user
     * Returns: global tags + user's own tags
     *
     * GET /api/subtask-tags
     */
    @GetMapping
    public ResponseEntity<List<SubtaskTagDTO>> getAllTags() {
        log.info("GET /api/subtask-tags - Getting all tags");
        Long userId = getCurrentUserId();
        List<SubtaskTagDTO> tags = tagService.getAllTagsForUser(userId);
        return ResponseEntity.ok(tags);
    }

    /**
     * Get all GLOBAL tags (for admin UI)
     *
     * GET /api/subtask-tags/global
     */
    @GetMapping("/global")
    public ResponseEntity<List<SubtaskTagDTO>> getGlobalTags() {
        log.info("GET /api/subtask-tags/global - Getting global tags");

        // Check admin role
        if (!isAdmin()) {
            log.warn("Non-admin user attempted to access global tags endpoint");
            return ResponseEntity.status(403).build();
        }

        List<SubtaskTagDTO> tags = tagService.getAllGlobalTags();
        return ResponseEntity.ok(tags);
    }

    /**
     * Get tag by ID
     *
     * GET /api/subtask-tags/{tagId}
     */
    @GetMapping("/{tagId}")
    public ResponseEntity<SubtaskTagDTO> getTagById(@PathVariable Long tagId) {
        log.info("GET /api/subtask-tags/{} - Getting tag", tagId);
        Long userId = getCurrentUserId();
        
        try {
            SubtaskTagDTO tag = tagService.getTagById(tagId, userId);
            return ResponseEntity.ok(tag);
        } catch (SecurityException e) {
            log.warn("Access denied for tagId={}, userId={}", tagId, userId);
            return ResponseEntity.status(403).build();
        }
    }

    /**
     * Create new tag
     *
     * POST /api/subtask-tags
     *
     * Request body:
     * {
     *   "name": "Machine Learning",
     *   "color": "#ff6b6b",
     *   "isGlobal": true  // Only admin can set true
     * }
     */
    @PostMapping
    public ResponseEntity<SubtaskTagDTO> createTag(@Valid @RequestBody CreateTagRequest request) {
        log.info("POST /api/subtask-tags - Creating tag: name={}, isGlobal={}",
                request.getName(), request.getIsGlobal());

        try {
            Long userId = getCurrentUserId();
            SubtaskTagDTO tag = tagService.createTag(request, userId);
            return ResponseEntity.ok(tag);
        } catch (SecurityException e) {
            log.warn("Security exception: {}", e.getMessage());
            return ResponseEntity.status(403).body(null);
        } catch (IllegalArgumentException e) {
            log.warn("Validation error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * Update tag
     *
     * PUT /api/subtask-tags/{tagId}
     *
     * Request body:
     * {
     *   "name": "Updated Name",
     *   "color": "#00ff00"
     * }
     */
    @PutMapping("/{tagId}")
    public ResponseEntity<SubtaskTagDTO> updateTag(
            @PathVariable Long tagId,
            @Valid @RequestBody CreateTagRequest request) {
        log.info("PUT /api/subtask-tags/{} - Updating tag", tagId);

        try {
            Long userId = getCurrentUserId();
            SubtaskTagDTO tag = tagService.updateTag(tagId, request, userId);
            return ResponseEntity.ok(tag);
        } catch (SecurityException e) {
            log.warn("Security exception: {}", e.getMessage());
            return ResponseEntity.status(403).body(null);
        } catch (IllegalArgumentException e) {
            log.warn("Validation error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * Delete tag
     *
     * DELETE /api/subtask-tags/{tagId}
     */
    @DeleteMapping("/{tagId}")
    public ResponseEntity<Void> deleteTag(@PathVariable Long tagId) {
        log.info("DELETE /api/subtask-tags/{} - Deleting tag", tagId);

        try {
            Long userId = getCurrentUserId();
            tagService.deleteTag(tagId, userId);
            return ResponseEntity.ok().build();
        } catch (SecurityException e) {
            log.warn("Security exception: {}", e.getMessage());
            return ResponseEntity.status(403).build();
        }
    }

    /**
     * Get tags by IDs (batch request)
     *
     * POST /api/subtask-tags/batch
     *
     * Request body: [1, 2, 3, 4]
     */
    @PostMapping("/batch")
    public ResponseEntity<List<SubtaskTagDTO>> getTagsByIds(@RequestBody List<Long> tagIds) {
        log.info("POST /api/subtask-tags/batch - Getting tags by IDs: {}", tagIds);
        Long userId = getCurrentUserId();
        List<SubtaskTagDTO> tags = tagService.getTagsByIds(tagIds, userId);
        return ResponseEntity.ok(tags);
    }

    // ===== Helper Methods =====

    /**
     * Get current user ID from security context
     */
    private Long getCurrentUserId() {
        String email = currentUser.getEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }

    /**
     * Check if current user is admin
     */
    private boolean isAdmin() {
        String email = currentUser.getEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getRole() == Role.ADMIN;
    }
}
