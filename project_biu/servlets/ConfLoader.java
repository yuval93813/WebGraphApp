package project_biu.servlets;

import project_biu.server.RequestInfo;
import project_biu.server.Servlet;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ConfLoader implements Servlet {

    @Override
    public void handle(RequestInfo ri, OutputStream toClient) throws IOException {
        // Get URI and extract filename
        String uri = ri.getUri();

        // Extract filename: URI is /conf/filename
        // Ensure there's something after "/conf/"
        String prefix = "/conf/";
        if (uri == null || !uri.startsWith(prefix) || uri.length() <= prefix.length()) {
            sendErrorResponse(toClient, 400, "Bad Request: Invalid URI format for configuration file.");
            return;
        }
        String fileName = uri.substring(prefix.length());
        if (fileName.isEmpty() || fileName.contains("/") || fileName.contains("..")) {
            sendErrorResponse(toClient, 400, "Bad Request: Invalid filename.");
            return;
        }

        // Construct path to the configuration file within the "config_files" directory
        Path projectRoot = Paths.get(".").toAbsolutePath().normalize(); // Assuming execution from project root
        Path filePath = projectRoot.resolve(Paths.get("config_files", fileName));

        // Read file content
        if (Files.exists(filePath) && Files.isReadable(filePath) && !Files.isDirectory(filePath)) {
            try {
                byte[] fileBytes = Files.readAllBytes(filePath);

                // Send headers
                String headers = "HTTP/1.1 200 OK\r\n" +
                                 "Content-Type: text/plain\r\n" +
                                 "Content-Length: " + fileBytes.length + "\r\n" +
                                 "\r\n";
                toClient.write(headers.getBytes("UTF-8"));

                // Send file content
                toClient.write(fileBytes);
            } catch (IOException e) {
                // Log the error (optional, depends on logging strategy)
                // System.err.println("Error reading file: " + filePath + " - " + e.getMessage());
                sendErrorResponse(toClient, 500, "Internal Server Error: Could not read file.");
            }
        } else {
            // File not found, not readable, or is a directory
            // System.err.println("File not found or not accessible: " + filePath);
            sendErrorResponse(toClient, 404, "Not Found: Configuration file '" + fileName + "' not found.");
        }
    }

    private void sendErrorResponse(OutputStream toClient, int statusCode, String statusMessage) throws IOException {
        String responseBody = statusCode + " " + statusMessage;
        String response = "HTTP/1.1 " + statusCode + " " + statusMessage + "\r\n" +
                          "Content-Type: text/plain\r\n" +
                          "Content-Length: " + responseBody.getBytes("UTF-8").length + "\r\n" +
                          "\r\n" +
                          responseBody;
        toClient.write(response.getBytes("UTF-8"));
    }

    @Override
    public void close() throws IOException {
        // No resources to close in this simple implementation
        // If we had file streams or other resources opened in a more complex servlet,
        // they would be closed here.
    }
}
