package servlets;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import server.RequestParser.RequestInfo;

/**
 * HtmlLoader servlet handles GET requests to /app/ endpoint.
 * It serves static HTML files from a specified directory,
 * extracts the filename from the URI, and returns the file content
 * or an error message if the file doesn't exist.
 */
public class HtmlLoader implements Servlet {
    
    private final String htmlDirectory;
    
    /**
     * Constructor that sets the HTML files directory
     * @param htmlDirectory The directory containing HTML files (relative or absolute path)
     */
    public HtmlLoader(String htmlDirectory) {
        this.htmlDirectory = htmlDirectory;
    }

    @Override
    public void handle(RequestInfo ri, OutputStream toClient) throws Exception {
        PrintWriter writer = new PrintWriter(toClient, true);
        
        try {
            // Extract filename from URI
            String uri = ri.getUri();
            String filename = extractFilename(uri);
            
            if (filename == null || filename.isEmpty()) {
                sendErrorResponse(writer, "No filename specified in request", 400);
                return;
            }
            
            // Construct file path
            Path filePath = Paths.get(htmlDirectory, filename);
            File file = filePath.toFile();
            
            // Security check: ensure file is within the allowed directory
            if (!isFileWithinDirectory(file, new File(htmlDirectory))) {
                sendErrorResponse(writer, "Access denied: File outside allowed directory", 403);
                return;
            }
            
            // Check if file exists and is readable
            if (!file.exists()) {
                sendErrorResponse(writer, "File not found: " + filename, 404);
                return;
            }
            
            if (!file.isFile()) {
                sendErrorResponse(writer, "Requested path is not a file: " + filename, 400);
                return;
            }
            
            if (!file.canRead()) {
                sendErrorResponse(writer, "File cannot be read: " + filename, 403);
                return;
            }
            
            // Read and serve the file
            byte[] fileContent = Files.readAllBytes(filePath);
            String contentType = determineContentType(filename);
            
            sendFileResponse(writer, toClient, fileContent, contentType);
            
        } catch (Exception e) {
            sendErrorResponse(writer, "Internal server error: " + e.getMessage(), 500);
        }
    }
    
    /**
     * Extracts filename from the URI path
     * For example: /app/index.html -> index.html
     */
    private String extractFilename(String uri) {
        // Remove query parameters if present
        int queryIndex = uri.indexOf('?');
        if (queryIndex != -1) {
            uri = uri.substring(0, queryIndex);
        }
        
        // Handle different URI patterns
        // Pattern 1: /app/filename.html
        if (uri.startsWith("/app/")) {
            String filename = uri.substring(5); // Remove "/app/"
            if (!filename.isEmpty()) {
                return filename;
            }
        }
        
        // Pattern 2: /app (no trailing slash) - default to index.html
        if (uri.equals("/app")) {
            return "index.html";
        }
        
        // Pattern 3: /app/ (with trailing slash) - default to index.html
        if (uri.equals("/app/")) {
            return "index.html";
        }
        
        // Fallback: extract last segment after any occurrence of "app"
        String[] segments = uri.split("/");
        for (int i = 0; i < segments.length - 1; i++) {
            if ("app".equals(segments[i]) && i + 1 < segments.length) {
                return segments[i + 1];
            }
        }
        
        return null;
    }
    
    /**
     * Security check to ensure file is within the allowed directory
     */
    private boolean isFileWithinDirectory(File file, File directory) throws IOException {
        String canonicalFilePath = file.getCanonicalPath();
        String canonicalDirPath = directory.getCanonicalPath();
        return canonicalFilePath.startsWith(canonicalDirPath + File.separator) || 
               canonicalFilePath.equals(canonicalDirPath);
    }
    
    /**
     * Determines content type based on file extension
     */
    private String determineContentType(String filename) {
        String lowerFilename = filename.toLowerCase();
        
        if (lowerFilename.endsWith(".html") || lowerFilename.endsWith(".htm")) {
            return "text/html; charset=UTF-8";
        } else if (lowerFilename.endsWith(".css")) {
            return "text/css; charset=UTF-8";
        } else if (lowerFilename.endsWith(".js")) {
            return "application/javascript; charset=UTF-8";
        } else if (lowerFilename.endsWith(".json")) {
            return "application/json; charset=UTF-8";
        } else if (lowerFilename.endsWith(".xml")) {
            return "application/xml; charset=UTF-8";
        } else if (lowerFilename.endsWith(".txt")) {
            return "text/plain; charset=UTF-8";
        } else if (lowerFilename.endsWith(".png")) {
            return "image/png";
        } else if (lowerFilename.endsWith(".jpg") || lowerFilename.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (lowerFilename.endsWith(".gif")) {
            return "image/gif";
        } else if (lowerFilename.endsWith(".svg")) {
            return "image/svg+xml";
        } else {
            return "application/octet-stream";
        }
    }
    
    /**
     * Sends the file content as HTTP response
     */
    private void sendFileResponse(PrintWriter writer, OutputStream toClient, byte[] fileContent, String contentType) throws IOException {
        writer.println("HTTP/1.1 200 OK");
        writer.println("Content-Type: " + contentType);
        writer.println("Content-Length: " + fileContent.length);
        writer.println("Connection: close");
        writer.println("Cache-Control: no-cache");
        writer.println();
        writer.flush();
        
        // Write binary content directly to OutputStream
        toClient.write(fileContent);
        toClient.flush();
    }
    
    /**
     * Sends an error response with appropriate HTTP status code
     */
    private void sendErrorResponse(PrintWriter writer, String errorMessage, int statusCode) {
        String statusText = getStatusText(statusCode);
        
        writer.println("HTTP/1.1 " + statusCode + " " + statusText);
        writer.println("Content-Type: text/html; charset=UTF-8");
        writer.println("Connection: close");
        writer.println();
        
        writer.println("<!DOCTYPE html>");
        writer.println("<html lang=\"en\">");
        writer.println("<head>");
        writer.println("    <meta charset=\"UTF-8\">");
        writer.println("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
        writer.println("    <title>Error " + statusCode + " - " + statusText + "</title>");
        writer.println("    <style>");
        writer.println("        body {");
        writer.println("            font-family: Arial, sans-serif;");
        writer.println("            margin: 0;");
        writer.println("            padding: 40px;");
        writer.println("            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);");
        writer.println("            min-height: 100vh;");
        writer.println("            display: flex;");
        writer.println("            align-items: center;");
        writer.println("            justify-content: center;");
        writer.println("        }");
        writer.println("        .error-container {");
        writer.println("            background: white;");
        writer.println("            padding: 40px;");
        writer.println("            border-radius: 10px;");
        writer.println("            box-shadow: 0 10px 25px rgba(0,0,0,0.2);");
        writer.println("            text-align: center;");
        writer.println("            max-width: 500px;");
        writer.println("        }");
        writer.println("        .error-code {");
        writer.println("            font-size: 4em;");
        writer.println("            font-weight: bold;");
        writer.println("            color: #dc3545;");
        writer.println("            margin: 0;");
        writer.println("        }");
        writer.println("        .error-title {");
        writer.println("            font-size: 1.5em;");
        writer.println("            margin: 10px 0;");
        writer.println("            color: #333;");
        writer.println("        }");
        writer.println("        .error-message {");
        writer.println("            color: #666;");
        writer.println("            margin: 20px 0;");
        writer.println("            line-height: 1.6;");
        writer.println("        }");
        writer.println("        .back-link {");
        writer.println("            display: inline-block;");
        writer.println("            margin-top: 20px;");
        writer.println("            padding: 10px 20px;");
        writer.println("            background: #007bff;");
        writer.println("            color: white;");
        writer.println("            text-decoration: none;");
        writer.println("            border-radius: 5px;");
        writer.println("            transition: background 0.3s;");
        writer.println("        }");
        writer.println("        .back-link:hover {");
        writer.println("            background: #0056b3;");
        writer.println("        }");
        writer.println("    </style>");
        writer.println("</head>");
        writer.println("<body>");
        writer.println("    <div class=\"error-container\">");
        writer.println("        <div class=\"error-code\">" + statusCode + "</div>");
        writer.println("        <div class=\"error-title\">" + statusText + "</div>");
        writer.println("        <div class=\"error-message\">" + escapeHtml(errorMessage) + "</div>");
        writer.println("        <a href=\"/app/index.html\" class=\"back-link\">← Back to Home</a>");
        writer.println("    </div>");
        writer.println("</body>");
        writer.println("</html>");
    }
    
    /**
     * Gets HTTP status text for common status codes
     */
    private String getStatusText(int statusCode) {
        switch (statusCode) {
            case 400: return "Bad Request";
            case 403: return "Forbidden";
            case 404: return "Not Found";
            case 500: return "Internal Server Error";
            default: return "Error";
        }
    }
    
    /**
     * Escapes HTML special characters
     */
    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;")
                  .replace("'", "&#x27;");
    }

    @Override
    public void close() throws IOException {
        // No resources to close
    }
}
