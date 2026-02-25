CREATE DATABASE task_manager;
USE task_manager;

CREATE TABLE tasks (
    id INT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    status ENUM('pending', 'in_progress', 'completed') DEFAULT 'pending',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Sample data
INSERT INTO tasks (title, description, status) VALUES
('Complete project', 'Finish the task management system', 'in_progress'),
('Review code', 'Check all the code for bugs', 'pending'),
('Write documentation', 'Create user documentation', 'pending');