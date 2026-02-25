import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.google.gson.Gson;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class Main {
    private static final Gson gson = new Gson();
    private static final TaskDAO taskDAO = new TaskDAO();

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 8080), 0);
        server.createContext("/api/tasks", new TaskHandler());
        server.setExecutor(null);
        server.start();
        
        System.out.println("Server started on http://localhost:8080");
        System.out.println("API endpoint: http://localhost:8080/api/tasks");
    }

    static class TaskHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // Set CORS headers
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");

            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            try {
                String method = exchange.getRequestMethod();
                String path = exchange.getRequestURI().getPath();
                String response = "";

                switch (method) {
                    case "GET":
                        if (path.equals("/api/tasks")) {
                            String query = exchange.getRequestURI().getQuery();
                            if (query != null && query.startsWith("status=")) {
                                String status = query.substring(7);
                                List<Task> tasks = taskDAO.getTasksByStatus(status);
                                response = gson.toJson(tasks);
                            } else {
                                List<Task> tasks = taskDAO.getAllTasks();
                                response = gson.toJson(tasks);
                            }
                        }
                        break;

                    case "POST":
                        if (path.equals("/api/tasks")) {
                            Task task = parseTaskFromRequest(exchange);
                            taskDAO.createTask(task);
                            response = "{\"message\":\"Task created successfully\"}";
                        }
                        break;

                    case "PUT":
                        String[] pathParts = path.split("/");
                        if (pathParts.length == 4 && pathParts[3].matches("\\d+")) {
                            int id = Integer.parseInt(pathParts[3]);
                            Task task = parseTaskFromRequest(exchange);
                            taskDAO.updateTask(id, task.getTitle());
                            response = "{\"message\":\"Task updated successfully\"}";
                        }
                        break;

                    case "DELETE":
                        pathParts = path.split("/");
                        if (pathParts.length == 4 && pathParts[3].matches("\\d+")) {
                            int id = Integer.parseInt(pathParts[3]);
                            taskDAO.deleteTask(id);
                            response = "{\"message\":\"Task deleted successfully\"}";
                        }
                        break;
                }

                sendResponse(exchange, 200, response);
            } catch (Exception e) {
                e.printStackTrace();
                String errorResponse = "{\"error\":\"" + e.getMessage() + "\"}";
                sendResponse(exchange, 500, errorResponse);
            }
        }

        private Task parseTaskFromRequest(HttpExchange exchange) throws IOException {
            InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
            try (BufferedReader br = new BufferedReader(isr)) {
                StringBuilder requestBody = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    requestBody.append(line);
                }
                return gson.fromJson(requestBody.toString(), Task.class);
            }
        }

        private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(statusCode, response.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
    }
}