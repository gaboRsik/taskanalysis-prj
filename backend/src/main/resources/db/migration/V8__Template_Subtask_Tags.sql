-- V8: Add tag support for template subtasks
-- This allows template creators to pre-assign tags to template subtasks,
-- which will be automatically applied when creating tasks from the template.

-- Junction table for many-to-many relationship between template_subtasks and subtask_tags
CREATE TABLE template_subtask_tag_mapping (
    template_subtask_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    PRIMARY KEY (template_subtask_id, tag_id),
    CONSTRAINT fk_template_subtask FOREIGN KEY (template_subtask_id) 
        REFERENCES template_subtasks(id) ON DELETE CASCADE,
    CONSTRAINT fk_template_tag FOREIGN KEY (tag_id) 
        REFERENCES subtask_tags(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Add index for reverse lookups
CREATE INDEX idx_template_tag_id ON template_subtask_tag_mapping(tag_id);
