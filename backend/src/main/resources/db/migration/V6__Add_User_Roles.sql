-- V6: Add role field to users table

ALTER TABLE users
ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'USER';

-- Create index on role for faster queries
CREATE INDEX idx_role ON users(role);

-- Set your admin account (replace with your actual email)
UPDATE users SET role = 'ADMIN' WHERE email = 'gaborsikdv1@gmail.com';
