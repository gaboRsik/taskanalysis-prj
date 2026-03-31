package com.taskanalysis.service;

import com.taskanalysis.dto.admin.UpdateRoleRequest;
import com.taskanalysis.dto.admin.UserDTO;
import com.taskanalysis.entity.Role;
import com.taskanalysis.entity.User;
import com.taskanalysis.exception.ResourceNotFoundException;
import com.taskanalysis.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AdminService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Get all users (ADMIN only)
     */
    public List<UserDTO> getAllUsers() {
        log.info("Fetching all users");
        return userRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Update user role (ADMIN only)
     */
    @Transactional
    public UserDTO updateUserRole(Long userId, UpdateRoleRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Role oldRole = user.getRole();
        user.setRole(request.getRole());
        
        User updatedUser = userRepository.save(user);
        
        log.info("User role updated: userId={}, oldRole={}, newRole={}", 
                userId, oldRole, request.getRole());

        return convertToDTO(updatedUser);
    }

    /**
     * Get user by ID (ADMIN only)
     */
    public UserDTO getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        return convertToDTO(user);
    }

    /**
     * Convert User entity to UserDTO
     */
    private UserDTO convertToDTO(User user) {
        return new UserDTO(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getRole(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
