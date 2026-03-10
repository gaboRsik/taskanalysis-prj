-- Add planned_total_time_minutes column to tasks table
ALTER TABLE tasks 
ADD COLUMN planned_total_time_minutes INT NULL
COMMENT 'Planned total time in minutes for the entire task';

-- Create index for better query performance
CREATE INDEX idx_planned_time ON tasks(planned_total_time_minutes);
