package com.taskanalysis.controller;

import com.taskanalysis.dto.timer.TimerResponse;
import com.taskanalysis.entity.User;
import com.taskanalysis.repository.UserRepository;
import com.taskanalysis.security.CurrentUser;
import com.taskanalysis.service.TimerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/timer")
public class TimerController {

    @Autowired
    private TimerService timerService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CurrentUser currentUser;

    @PostMapping("/start/{subtaskId}")
    public ResponseEntity<TimerResponse> startTimer(@PathVariable Long subtaskId) {
        Long userId = getCurrentUserId();
        TimerResponse response = timerService.startTimer(userId, subtaskId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/stop")
    public ResponseEntity<TimerResponse> stopTimer() {
        Long userId = getCurrentUserId();
        TimerResponse response = timerService.stopCurrentTimer(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/active")
    public ResponseEntity<TimerResponse> getActiveTimer() {
        Long userId = getCurrentUserId();
        TimerResponse response = timerService.getActiveTimerForUser(userId);
        if (response == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(response);
    }

    private Long getCurrentUserId() {
        String email = currentUser.getEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }

}
