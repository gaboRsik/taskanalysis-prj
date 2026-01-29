package com.taskanalysis.controller;

import com.taskanalysis.dto.subtask.SubtaskRequest;
import com.taskanalysis.dto.subtask.SubtaskResponse;
import com.taskanalysis.entity.User;
import com.taskanalysis.repository.UserRepository;
import com.taskanalysis.security.CurrentUser;
import com.taskanalysis.service.SubtaskService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/subtasks")
public class SubtaskController {

    @Autowired
    private SubtaskService subtaskService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CurrentUser currentUser;

    @GetMapping("/{id}")
    public ResponseEntity<SubtaskResponse> getSubtaskById(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        SubtaskResponse subtask = subtaskService.getSubtaskById(userId, id);
        return ResponseEntity.ok(subtask);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SubtaskResponse> updateSubtask(
            @PathVariable Long id,
            @Valid @RequestBody SubtaskRequest request) {
        Long userId = getCurrentUserId();
        SubtaskResponse response = subtaskService.updateSubtask(userId, id, request);
        return ResponseEntity.ok(response);
    }

    private Long getCurrentUserId() {
        String email = currentUser.getEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }

}
