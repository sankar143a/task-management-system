import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/api/tasks/*")
public class TaskServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private TaskDAO taskDAO;
    private Gson gson;

    @Override
    public void init() throws ServletException {
        super.init();
        taskDAO = new TaskDAO();
        gson = new Gson();
    }

    // Handle GET requests - Get all tasks or tasks by status
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        // Set CORS headers
        setCorsHeaders(response);
        
        String pathInfo = request.getPathInfo();
        PrintWriter out = response.getWriter();
        
        try {
            // Check if there's a specific task ID in the path
            if (pathInfo != null && pathInfo.length() > 1) {
                // Get single task by ID
                String[] pathParts = pathInfo.split("/");
                if (pathParts.length == 2) {
                    int taskId = Integer.parseInt(pathParts[1]);
                    // Since we don't have getTaskById method, we'll get all and filter
                    // You can add getTaskById method to TaskDAO if needed
                    List<Task> tasks = taskDAO.getAllTasks();
                    Task foundTask = tasks.stream()
                            .filter(task -> task.getId() == taskId)
                            .findFirst()
                            .orElse(null);
                    
                    if (foundTask != null) {
                        out.print(gson.toJson(foundTask));
                        response.setStatus(HttpServletResponse.SC_OK);
                    } else {
                        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        out.print("{\"error\":\"Task not found\"}");
                    }
                }
            } else {
                // Get status parameter if present
                String status = request.getParameter("status");
                List<Task> tasks;
                
                if (status != null && !status.isEmpty()) {
                    tasks = taskDAO.getTasksByStatus(status);
                } else {
                    tasks = taskDAO.getAllTasks();
                }
                
                out.print(gson.toJson(tasks));
                response.setStatus(HttpServletResponse.SC_OK);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\":\"Database error: " + e.getMessage() + "\"}");
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"Invalid task ID format\"}");
        }
        
        out.flush();
    }

    // Handle POST requests - Create new task
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        // Set CORS headers
        setCorsHeaders(response);
        
        PrintWriter out = response.getWriter();
        
        try {
            // Read request body
            BufferedReader reader = request.getReader();
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            
            // Parse JSON to Task object
            Task task = gson.fromJson(sb.toString(), Task.class);
            
            // Validate task data
            if (task.getTitle() == null || task.getTitle().trim().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Task title is required\"}");
                out.flush();
                return;
            }
            
            // Set default status if not provided
            if (task.getStatus() == null || task.getStatus().isEmpty()) {
                task.setStatus("pending");
            }
            
            // Create task in database
            taskDAO.createTask(task);
            
            // Return success response
            response.setStatus(HttpServletResponse.SC_CREATED);
            out.print("{\"message\":\"Task created successfully\"}");
            
        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\":\"Database error: " + e.getMessage() + "\"}");
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"Invalid request: " + e.getMessage() + "\"}");
        }
        
        out.flush();
    }

    // Handle PUT requests - Update task
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        // Set CORS headers
        setCorsHeaders(response);
        
        PrintWriter out = response.getWriter();
        String pathInfo = request.getPathInfo();
        
        try {
            // Check if task ID is provided in path
            if (pathInfo == null || pathInfo.length() <= 1) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Task ID is required\"}");
                out.flush();
                return;
            }
            
            String[] pathParts = pathInfo.split("/");
            if (pathParts.length != 2) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Invalid path format\"}");
                out.flush();
                return;
            }
            
            int taskId = Integer.parseInt(pathParts[1]);
            
            // Read request body
            BufferedReader reader = request.getReader();
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            
            // Parse JSON
            JsonObject jsonObject = JsonParser.parseString(sb.toString()).getAsJsonObject();
            
            // Get title from JSON
            if (!jsonObject.has("title") || jsonObject.get("title").isJsonNull()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Task title is required\"}");
                out.flush();
                return;
            }
            
            String title = jsonObject.get("title").getAsString();
            
            // Update task in database
            taskDAO.updateTask(taskId, title);
            
            // Return success response
            response.setStatus(HttpServletResponse.SC_OK);
            out.print("{\"message\":\"Task updated successfully\"}");
            
        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\":\"Database error: " + e.getMessage() + "\"}");
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"Invalid task ID format\"}");
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"Invalid request: " + e.getMessage() + "\"}");
        }
        
        out.flush();
    }

    // Handle DELETE requests - Delete task
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        // Set CORS headers
        setCorsHeaders(response);
        
        PrintWriter out = response.getWriter();
        String pathInfo = request.getPathInfo();
        
        try {
            // Check if task ID is provided in path
            if (pathInfo == null || pathInfo.length() <= 1) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Task ID is required\"}");
                out.flush();
                return;
            }
            
            String[] pathParts = pathInfo.split("/");
            if (pathParts.length != 2) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Invalid path format\"}");
                out.flush();
                return;
            }
            
            int taskId = Integer.parseInt(pathParts[1]);
            
            // Delete task from database
            taskDAO.deleteTask(taskId);
            
            // Return success response
            response.setStatus(HttpServletResponse.SC_OK);
            out.print("{\"message\":\"Task deleted successfully\"}");
            
        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\":\"Database error: " + e.getMessage() + "\"}");
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"Invalid task ID format\"}");
        }
        
        out.flush();
    }

    // Handle OPTIONS requests for CORS preflight
    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        setCorsHeaders(response);
        response.setStatus(HttpServletResponse.SC_OK);
    }

    // Helper method to set CORS headers
    private void setCorsHeaders(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        response.setHeader("Access-Control-Max-Age", "3600");
    }
}