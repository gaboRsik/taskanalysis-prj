-- V7: Subtask Tags - Dual-Level Tag System (Global + User-specific)

-- Create subtask_tags table
CREATE TABLE subtask_tags (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    color VARCHAR(7) NOT NULL DEFAULT '#667eea',  -- HEX color
    is_global BOOLEAN DEFAULT FALSE,              -- TRUE = global tag (admin), FALSE = user-specific
    user_id BIGINT,                               -- NULL for global tags, user_id for user-specific tags
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by_user_id BIGINT NOT NULL,           -- Who created this tag (admin or user)
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by_user_id) REFERENCES users(id),
    
    -- Unique constraint: tag name must be unique within scope
    -- Global tags: name must be unique globally
    -- User-specific tags: name must be unique per user
    UNIQUE KEY unique_tag_scope (name, is_global, user_id)
);

-- Create subtask_tag_mapping table (many-to-many)
CREATE TABLE subtask_tag_mapping (
    subtask_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    PRIMARY KEY (subtask_id, tag_id),
    FOREIGN KEY (subtask_id) REFERENCES subtasks(id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id) REFERENCES subtask_tags(id) ON DELETE CASCADE
);

-- Indexes for performance
CREATE INDEX idx_subtask_tags_global ON subtask_tags(is_global);
CREATE INDEX idx_subtask_tags_user ON subtask_tags(user_id);
CREATE INDEX idx_subtask_tags_name ON subtask_tags(name);
CREATE INDEX idx_mapping_subtask ON subtask_tag_mapping(subtask_id);
CREATE INDEX idx_mapping_tag ON subtask_tag_mapping(tag_id);

-- Seed GLOBAL tags (created by admin user)
-- Note: Assuming admin user ID is 1 (adjust if different)
INSERT INTO subtask_tags (name, color, is_global, user_id, created_by_user_id) 
SELECT 'Frontend', '#3b82f6', TRUE, NULL, u.id FROM users u WHERE u.role = 'ADMIN' LIMIT 1;

INSERT INTO subtask_tags (name, color, is_global, user_id, created_by_user_id) 
SELECT 'Backend', '#10b981', TRUE, NULL, u.id FROM users u WHERE u.role = 'ADMIN' LIMIT 1;

INSERT INTO subtask_tags (name, color, is_global, user_id, created_by_user_id) 
SELECT 'Database', '#f59e0b', TRUE, NULL, u.id FROM users u WHERE u.role = 'ADMIN' LIMIT 1;

INSERT INTO subtask_tags (name, color, is_global, user_id, created_by_user_id) 
SELECT 'API', '#8b5cf6', TRUE, NULL, u.id FROM users u WHERE u.role = 'ADMIN' LIMIT 1;

INSERT INTO subtask_tags (name, color, is_global, user_id, created_by_user_id) 
SELECT 'Testing', '#ef4444', TRUE, NULL, u.id FROM users u WHERE u.role = 'ADMIN' LIMIT 1;

INSERT INTO subtask_tags (name, color, is_global, user_id, created_by_user_id) 
SELECT 'Design', '#ec4899', TRUE, NULL, u.id FROM users u WHERE u.role = 'ADMIN' LIMIT 1;

INSERT INTO subtask_tags (name, color, is_global, user_id, created_by_user_id) 
SELECT 'DevOps', '#6366f1', TRUE, NULL, u.id FROM users u WHERE u.role = 'ADMIN' LIMIT 1;

INSERT INTO subtask_tags (name, color, is_global, user_id, created_by_user_id) 
SELECT 'Deployment', '#14b8a6', TRUE, NULL, u.id FROM users u WHERE u.role = 'ADMIN' LIMIT 1;

INSERT INTO subtask_tags (name, color, is_global, user_id, created_by_user_id) 
SELECT 'Documentation', '#f97316', TRUE, NULL, u.id FROM users u WHERE u.role = 'ADMIN' LIMIT 1;

INSERT INTO subtask_tags (name, color, is_global, user_id, created_by_user_id) 
SELECT 'Bugfix', '#dc2626', TRUE, NULL, u.id FROM users u WHERE u.role = 'ADMIN' LIMIT 1;

INSERT INTO subtask_tags (name, color, is_global, user_id, created_by_user_id) 
SELECT 'Refactoring', '#7c3aed', TRUE, NULL, u.id FROM users u WHERE u.role = 'ADMIN' LIMIT 1;

INSERT INTO subtask_tags (name, color, is_global, user_id, created_by_user_id) 
SELECT 'Research', '#06b6d4', TRUE, NULL, u.id FROM users u WHERE u.role = 'ADMIN' LIMIT 1;
