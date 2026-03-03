-- Add task_count column to task_templates table
ALTER TABLE task_templates 
ADD COLUMN task_count INT NOT NULL DEFAULT 1 
COMMENT 'Number of tasks to create from this template';

-- Update existing records to have default task_count = 1
UPDATE task_templates SET task_count = 1 WHERE task_count IS NULL;
