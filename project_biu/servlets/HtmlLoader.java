package project_biu.servlets;

import project_biu.server.RequestInfo;
import project_biu.server.Servlet;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;

public class HtmlLoader implements Servlet {

    @Override
    public void handle(RequestInfo ri, OutputStream toClient) throws IOException {
        // Get URI and extract filename
        String uri = ri.getUri();

        // Extract filename: URI is /html/filename
        String prefix = "/html/";
        if (uri == null || !uri.startsWith(prefix) || uri.length() <= prefix.length()) {
            sendErrorResponse(toClient, 400, "Bad Request: Invalid URI format for HTML file.");
            return;
        }
        String fileName = uri.substring(prefix.length());

        // Basic security check for filename (prevent directory traversal, etc.)
        if (fileName.isEmpty() || fileName.contains("/") || fileName.contains("..")) {
            sendErrorResponse(toClient, 400, "Bad Request: Invalid HTML filename.");
            return;
        }

        // Ensure the file has an HTML extension (optional, but good practice)
        if (!fileName.toLowerCase().endsWith(".html") && !fileName.toLowerCase().endsWith(".htm")) {
            sendErrorResponse(toClient, 400, "Bad Request: File must be an HTML file (.html or .htm).");
            return;
        }

        // Construct path to the HTML file within the "html_files" directory
        Path projectRoot = Paths.get(".").toAbsolutePath().normalize(); // Assuming execution from project root
        Path filePath = projectRoot.resolve(Paths.get("html_files", fileName));

        // Read file content
        if (Files.exists(filePath) && Files.isReadable(filePath) && !Files.isDirectory(filePath)) {
            try {
                byte[] fileBytes = Files.readAllBytes(filePath);

                // Send headers
                String headers = "HTTP/1.1 200 OK\r\n" +
                                 "Content-Type: text/html; charset=UTF-8\r\n" + // Specify UTF-8 for HTML
                                 "Content-Length: " + fileBytes.length + "\r\n" +
                                 "\r\n";
                toClient.write(headers.getBytes(StandardCharsets.UTF_8));

                // Send file content
                toClient.write(fileBytes);
            } catch (IOException e) {
                // Log the error (optional, depends on logging strategy)
                // System.err.println("Error reading file: " + filePath + " - " + e.getMessage());
                sendErrorResponse(toClient, 500, "Internal Server Error: Could not read HTML file.");
            }
        } else {
            // File not found, not readable, or is a directory
            // System.err.println("File not found or not accessible: " + filePath);
            sendErrorResponse(toClient, 404, "Not Found: HTML file '" + fileName + "' not found.");
        }
    }

    private void sendErrorResponse(OutputStream toClient, int statusCode, String statusMessage) throws IOException {
        String responseBody = statusCode + " " + statusMessage;
        String httpResponse = "HTTP/1.1 " + statusCode + " " + statusMessage + "\r\n" +
                              "Content-Type: text/html; charset=UTF-8\r\n" + // Error page can also be HTML
                              "Content-Length: " + responseBody.getBytes(StandardCharsets.UTF_8).length + "\r\n" +
                              "\r\n" +
                              responseBody; // Simple text error, could be HTML formatted
        toClient.write(httpResponse.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void close() throws IOException {
        // No resources to close in this simple implementation.
        // If file streams or other closeable resources were opened and held by the servlet instance,
        // they would be cleaned up here.
    }
}
