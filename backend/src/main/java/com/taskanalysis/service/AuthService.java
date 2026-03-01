package com.taskanalysis.service;

import com.taskanalysis.dto.auth.AuthResponse;
import com.taskanalysis.dto.auth.LoginRequest;
import com.taskanalysis.dto.auth.RegisterRequest;
import com.taskanalysis.entity.User;
import com.taskanalysis.exception.AccountLockedException;
import com.taskanalysis.exception.BusinessException;
import com.taskanalysis.exception.ResourceNotFoundException;
import com.taskanalysis.repository.UserRepository;
import com.taskanalysis.security.JwtTokenProvider;
import com.taskanalysis.security.LoginAttemptService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private LoginAttemptService loginAttemptService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email already registered");
        }

        // Create new user
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        User savedUser = userRepository.save(user);

        log.info("New user registered: {}", savedUser.getEmail());

        // Generate tokens
        String accessToken = tokenProvider.generateAccessToken(savedUser.getEmail());
        String refreshToken = tokenProvider.generateRefreshToken(savedUser.getEmail());

        return new AuthResponse(
                accessToken,
                refreshToken,
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getName()
        );
    }

    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail();
        
        // Check if account is locked
        if (loginAttemptService.isLocked(email)) {
            long remainingMinutes = loginAttemptService.getRemainingLockoutMinutes(email);
            log.warn("Login attempt on locked account: {}", email);
            throw new AccountLockedException(
                String.format("Account is temporarily locked due to too many failed login attempts. Please try again in %d minutes.", remainingMinutes),
                remainingMinutes
            );
        }

        try {
            // Authenticate user
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            email,
                            request.getPassword()
                    )
            );

            // Get user
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

            // Clear failed attempts on successful login
            loginAttemptService.loginSucceeded(email);

            log.info("Successful login: {}", email);

            // Generate tokens
            String accessToken = tokenProvider.generateAccessToken(user.getEmail());
            String refreshToken = tokenProvider.generateRefreshToken(user.getEmail());

            return new AuthResponse(
                    accessToken,
                    refreshToken,
                    user.getId(),
                    user.getEmail(),
                    user.getName()
            );
        } catch (BadCredentialsException ex) {
            // Record failed attempt
            loginAttemptService.loginFailed(email);
            
            int remainingAttempts = loginAttemptService.getRemainingAttempts(email);
            log.warn("Failed login attempt for: {}. Remaining attempts: {}", email, remainingAttempts);
            
            if (remainingAttempts > 0) {
                throw new BadCredentialsException(
                    String.format("Invalid credentials. %d attempts remaining before account lockout.", remainingAttempts)
                );
            } else {
                throw new AccountLockedException(
                    "Too many failed login attempts. Account is now locked for 15 minutes.",
                    15
                );
            }
        }
    }

}
