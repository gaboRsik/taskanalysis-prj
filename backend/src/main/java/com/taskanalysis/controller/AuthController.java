package com.taskanalysis.controller;

import com.taskanalysis.dto.auth.AuthResponse;
import com.taskanalysis.dto.auth.ChangePasswordRequest;
import com.taskanalysis.dto.auth.ChangePasswordResponse;
import com.taskanalysis.dto.auth.ForgotPasswordRequest;
import com.taskanalysis.dto.auth.LoginRequest;
import com.taskanalysis.dto.auth.MessageResponse;
import com.taskanalysis.dto.auth.RegisterRequest;
import com.taskanalysis.dto.auth.ResetPasswordRequest;
import com.taskanalysis.security.CurrentUser;
import com.taskanalysis.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private CurrentUser currentUser;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/change-password")
    public ResponseEntity<ChangePasswordResponse> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        String email = currentUser.getEmail();
        ChangePasswordResponse response = authService.changePassword(email, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        MessageResponse response = authService.forgotPassword(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        MessageResponse response = authService.resetPassword(request);
        return ResponseEntity.ok(response);
    }

}
