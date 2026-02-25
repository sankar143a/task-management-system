// CONNECTION TO BACKEND - THIS IS THE KEY PART
const API_BASE_URL = 'http://localhost:8080/api/tasks';

// Load tasks when page loads
document.addEventListener('DOMContentLoaded', loadTasks);

// Load all tasks from backend
async function loadTasks() {
    try {
        const response = await fetch(API_BASE_URL);
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        const tasks = await response.json();
        displayTasks(tasks);
    } catch (error) {
        console.error('Error loading tasks:', error);
        document.getElementById('tasksList').innerHTML = 
            '<p class="no-tasks">Error loading tasks. Make sure backend is running on port 8080</p>';
    }
}

// Display tasks in the UI
function displayTasks(tasks) {
    const tasksList = document.getElementById('tasksList');
    tasksList.innerHTML = '';

    if (tasks.length === 0) {
        tasksList.innerHTML = '<p class="no-tasks">No tasks found</p>';
        return;
    }

    tasks.forEach(task => {
        const taskElement = createTaskElement(task);
        tasksList.appendChild(taskElement);
    });
}

// Create task HTML element
function createTaskElement(task) {
    const div = document.createElement('div');
    div.className = 'task-item';
    div.dataset.id = task.id;
    div.dataset.status = task.status;

    const statusClass = `status-${task.status.replace('_', '')}`;
    const statusText = task.status.replace('_', ' ');

    div.innerHTML = `
        <div class="task-info">
            <h3>${escapeHtml(task.title)}</h3>
            <p>${escapeHtml(task.description) || 'No description'}</p>
            <span class="task-status ${statusClass}">${statusText}</span>
        </div>
        <div class="task-actions">
            <button class="edit-btn" onclick="editTask(${task.id})">✏️</button>
            <button class="delete-btn" onclick="deleteTask(${task.id})">🗑️</button>
        </div>
    `;

    return div;
}

// Helper function to escape HTML
function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

// Add new task
async function addTask() {
    const title = document.getElementById('taskTitle').value;
    const description = document.getElementById('taskDescription').value;
    const status = document.getElementById('taskStatus').value;

    if (!title) {
        alert('Please enter a task title');
        return;
    }

    try {
        const response = await fetch(API_BASE_URL, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ title, description, status })
        });

        if (response.ok) {
            clearForm();
            loadTasks();
            alert('Task added successfully');
        } else {
            throw new Error('Failed to add task');
        }
    } catch (error) {
        console.error('Error adding task:', error);
        alert('Error adding task');
    }
}

// Delete task
async function deleteTask(id) {
    if (!confirm('Are you sure you want to delete this task?')) return;

    try {
        const response = await fetch(`${API_BASE_URL}/${id}`, {
            method: 'DELETE'
        });

        if (response.ok) {
            loadTasks();
            alert('Task deleted successfully');
        } else {
            throw new Error('Failed to delete task');
        }
    } catch (error) {
        console.error('Error deleting task:', error);
        alert('Error deleting task');
    }
}

// Edit task
async function editTask(id) {
    const newTitle = prompt('Enter new task title:');
    if (!newTitle) return;

    try {
        const response = await fetch(`${API_BASE_URL}/${id}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ title: newTitle })
        });

        if (response.ok) {
            loadTasks();
            alert('Task updated successfully');
        } else {
            throw new Error('Failed to update task');
        }
    } catch (error) {
        console.error('Error updating task:', error);
        alert('Error updating task');
    }
}

// Filter tasks by status
async function filterTasks(status) {
    try {
        const url = status === 'all' ? API_BASE_URL : `${API_BASE_URL}?status=${status}`;
        const response = await fetch(url);
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        const tasks = await response.json();
        displayTasks(tasks);
    } catch (error) {
        console.error('Error filtering tasks:', error);
        alert('Error filtering tasks');
    }
}

// Clear form fields
function clearForm() {
    document.getElementById('taskTitle').value = '';
    document.getElementById('taskDescription').value = '';
    document.getElementById('taskStatus').value = 'pending';
}