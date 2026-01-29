package com.taskanalysis.controller;

import com.taskanalysis.dto.category.CategoryRequest;
import com.taskanalysis.dto.category.CategoryResponse;
import com.taskanalysis.entity.User;
import com.taskanalysis.repository.UserRepository;
import com.taskanalysis.security.CurrentUser;
import com.taskanalysis.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CurrentUser currentUser;

    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CategoryRequest request) {
        Long userId = getCurrentUserId();
        CategoryResponse response = categoryService.createCategory(userId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getUserCategories() {
        Long userId = getCurrentUserId();
        List<CategoryResponse> categories = categoryService.getUserCategories(userId);
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        CategoryResponse category = categoryService.getCategoryById(userId, id);
        return ResponseEntity.ok(category);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request) {
        Long userId = getCurrentUserId();
        CategoryResponse response = categoryService.updateCategory(userId, id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        categoryService.deleteCategory(userId, id);
        return ResponseEntity.noContent().build();
    }

    private Long getCurrentUserId() {
        String email = currentUser.getEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }

}
