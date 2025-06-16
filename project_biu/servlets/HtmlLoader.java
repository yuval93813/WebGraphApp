package servlets;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import server.RequestParser.RequestInfo;

/**
 * HtmlLoader servlet that serves static HTML files and other web resources.
 * This servlet handles requests for HTML files, CSS, JavaScript, and other static content.
 */
public class HtmlLoader implements Servlet {
    
    private final String basePath;
    
    /**
     * Creates a new HtmlLoader with the specified base path for serving files.
     * 
     * @param basePath The base directory path where HTML files are located
     */
    public HtmlLoader(String basePath) {
        this.basePath = basePath;
    }

    /**
     * Handles HTTP requests for static files.
     * 
     * @param ri The request information containing HTTP details
     * @param toClient The output stream to send the response
     * @throws Exception If an error occurs during request processing
     */
    @Override
    public void handle(RequestInfo ri, OutputStream toClient) throws Exception {
        try {
            String uri = ri.getUri();
            
            // Remove the servlet path prefix (e.g., "/app/") to get the file path
            String filePath = extractFilePath(uri);
            
            // Default to index.html if no specific file is requested
            if (filePath.isEmpty() || filePath.equals("/")) {
                filePath = "index.html";
            }
            
            // Construct the full file path
            File file = new File(basePath, filePath);
            
            // Check if file exists and is readable
            if (file.exists() && file.isFile() && file.canRead()) {
                serveFile(file, toClient);
            } else {
                sendNotFound(toClient, "File not found: " + filePath);
            }
            
        } catch (Exception e) {
            sendError(toClient, "Internal server error: " + e.getMessage());
        }
    }
    
    /**
     * Extracts the file path from the URI by removing the servlet path prefix.
     * 
     * @param uri The request URI
     * @return The file path relative to the base directory
     */
    private String extractFilePath(String uri) {
        // Remove leading "/app/" or similar servlet path
        if (uri.startsWith("/app/")) {
            return uri.substring(5); // Remove "/app/"
        }
        return uri.substring(1); // Remove leading "/"
    }
    
    /**
     * Serves a file to the client with appropriate HTTP headers.
     * 
     * @param file The file to serve
     * @param toClient The output stream to send the response
     * @throws IOException If an I/O error occurs
     */
    private void serveFile(File file, OutputStream toClient) throws IOException {
        String contentType = getContentType(file.getName());
        long contentLength = file.length();
        
        // Send HTTP headers
        PrintWriter writer = new PrintWriter(toClient, true);
        writer.println("HTTP/1.1 200 OK");
        writer.println("Content-Type: " + contentType);
        writer.println("Content-Length: " + contentLength);
        writer.println(); // Empty line to separate headers from body
        writer.flush();
        
        // Send file content
        try (FileInputStream fileInput = new FileInputStream(file)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fileInput.read(buffer)) != -1) {
                toClient.write(buffer, 0, bytesRead);
            }
        }
        toClient.flush();
    }
    
    /**
     * Determines the MIME content type based on file extension.
     * 
     * @param fileName The name of the file
     * @return The MIME content type
     */
    private String getContentType(String fileName) {
        String lowerCaseName = fileName.toLowerCase();
        
        if (lowerCaseName.endsWith(".html") || lowerCaseName.endsWith(".htm")) {
            return "text/html; charset=UTF-8";
        } else if (lowerCaseName.endsWith(".css")) {
            return "text/css";
        } else if (lowerCaseName.endsWith(".js")) {
            return "application/javascript";
        } else if (lowerCaseName.endsWith(".json")) {
            return "application/json";
        } else if (lowerCaseName.endsWith(".png")) {
            return "image/png";
        } else if (lowerCaseName.endsWith(".jpg") || lowerCaseName.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (lowerCaseName.endsWith(".gif")) {
            return "image/gif";
        } else if (lowerCaseName.endsWith(".svg")) {
            return "image/svg+xml";
        } else if (lowerCaseName.endsWith(".ico")) {
            return "image/x-icon";
        } else {
            return "text/plain";
        }
    }
    
    /**
     * Sends a 404 Not Found response.
     * 
     * @param toClient The output stream to send the response
     * @param message The error message to include
     * @throws IOException If an I/O error occurs
     */
    private void sendNotFound(OutputStream toClient, String message) throws IOException {
        String htmlContent = generateErrorPage("404 Not Found", message);
        sendHttpResponse(toClient, htmlContent, "404 Not Found");
    }
    
    /**
     * Sends a 500 Internal Server Error response.
     * 
     * @param toClient The output stream to send the response
     * @param message The error message to include
     * @throws IOException If an I/O error occurs
     */
    private void sendError(OutputStream toClient, String message) throws IOException {
        String htmlContent = generateErrorPage("500 Internal Server Error", message);
        sendHttpResponse(toClient, htmlContent, "500 Internal Server Error");
    }
    
    /**
     * Generates an HTML error page.
     * 
     * @param title The error title
     * @param message The error message
     * @return HTML string for the error page
     */
    private String generateErrorPage(String title, String message) {
        return "<!DOCTYPE html>\n" +
               "<html lang=\"en\">\n" +
               "<head>\n" +
               "    <meta charset=\"UTF-8\">\n" +
               "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
               "    <title>" + escapeHtml(title) + "</title>\n" +
               "    <style>\n" +
               "        body { font-family: Arial, sans-serif; padding: 20px; background-color: #f9f9f9; text-align: center; }\n" +
               "        .container { background: white; border-radius: 8px; padding: 40px; margin: 50px auto; max-width: 600px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }\n" +
               "        h1 { color: #721c24; margin-bottom: 20px; }\n" +
               "        p { color: #666; font-size: 16px; }\n" +
               "    </style>\n" +
               "</head>\n" +
               "<body>\n" +
               "    <div class=\"container\">\n" +
               "        <h1>" + escapeHtml(title) + "</h1>\n" +
               "        <p>" + escapeHtml(message) + "</p>\n" +
               "    </div>\n" +
               "</body>\n" +
               "</html>";
    }
    
    /**
     * Sends an HTTP response with the given content and status.
     * 
     * @param toClient The output stream to send the response
     * @param htmlContent The HTML content to send
     * @param status The HTTP status line
     * @throws IOException If an I/O error occurs
     */
    private void sendHttpResponse(OutputStream toClient, String htmlContent, String status) throws IOException {
        PrintWriter writer = new PrintWriter(toClient, true);
        
        writer.println("HTTP/1.1 " + status);
        writer.println("Content-Type: text/html; charset=UTF-8");
        writer.println("Content-Length: " + htmlContent.getBytes("UTF-8").length);
        writer.println(); // Empty line to separate headers from body
        
        writer.print(htmlContent);
        writer.flush();
    }
    
    /**
     * Escapes HTML special characters to prevent XSS attacks.
     * 
     * @param text The text to escape
     * @return HTML-escaped text
     */
    private String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;")
                  .replace("'", "&#39;");
    }

    /**
     * Closes the servlet and releases any resources.
     */
    @Override
    public void close() throws IOException {
        // No resources to close in this implementation
    }
}