-- Task Templates Feature
-- Version: 2.0.0
-- Date: 2026-03-03
-- Description: Add task template functionality for quick task creation

-- Task Templates table
-- Stores reusable task templates with predefined structure
CREATE TABLE task_templates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL COMMENT 'Category for tasks created from this template (required for analytics)',
    name VARCHAR(255) NOT NULL COMMENT 'Template name',
    description TEXT NULL COMMENT 'Template description',
    subtask_count INT NOT NULL DEFAULT 1 COMMENT 'Number of subtasks in this template',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_category_id (category_id),
    UNIQUE KEY unique_user_category_template (user_id, category_id, name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Reusable task templates for structured task creation with mandatory category';

-- Template Subtasks table
-- Stores predefined subtasks with planned points for templates
CREATE TABLE template_subtasks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    template_id BIGINT NOT NULL,
    subtask_number INT NOT NULL COMMENT 'Subtask order/number (1, 2, 3...)',
    planned_points INT NULL COMMENT 'Default planned points for this subtask',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (template_id) REFERENCES task_templates(id) ON DELETE CASCADE,
    INDEX idx_template_id (template_id),
    UNIQUE KEY unique_template_subtask (template_id, subtask_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Predefined subtasks for task templates';
