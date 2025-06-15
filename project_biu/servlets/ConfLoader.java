package servlets;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import configs.GenericConfig;
import graph.Graph;
import server.RequestParser.RequestInfo;
import views.HtmlGraphWriter;

/**
 * ConfLoader servlet handles POST requests to /upload endpoint.
 * It extracts uploaded files, stores them on the server,
 * parses them into GenericConfig, creates a Graph object,
 * and returns HTML displaying the computation graph information.
 */
public class ConfLoader implements Servlet {
    
    private static final String UPLOAD_DIRECTORY = "uploaded_configs";
    
    public ConfLoader() {
        // Create upload directory if it doesn't exist
        File uploadDir = new File(UPLOAD_DIRECTORY);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
    }

    @Override
    public void handle(RequestInfo ri, OutputStream toClient) throws Exception {
        PrintWriter writer = new PrintWriter(toClient, true);
        
        // Add CORS headers to allow iframe requests
        writer.println("HTTP/1.1 200 OK");
        writer.println("Content-Type: text/html; charset=UTF-8");
        writer.println("Access-Control-Allow-Origin: *");
        writer.println("Access-Control-Allow-Methods: POST, GET, OPTIONS");
        writer.println("Access-Control-Allow-Headers: Content-Type");
        writer.println("Connection: close");
        writer.println();
        
        try {
            // Extract file content from the request
            byte[] content = ri.getContent();
            String contentString = new String(content, StandardCharsets.UTF_8);
            
            if (content.length == 0) {
                sendErrorResponse(writer, "No file content received");
                return;
            }
            
            // Parse multipart form data to extract file
            FileInfo fileInfo = parseMultipartData(contentString);
            
            if (fileInfo == null || fileInfo.content.isEmpty()) {
                sendErrorResponse(writer, "No valid file found in upload");
                return;
            }
            
            // Generate unique filename and save file
            String filename = generateUniqueFilename(fileInfo.filename);
            File savedFile = saveFile(filename, fileInfo.content);
            
            // Parse configuration and create graph
            GenericConfig config = new GenericConfig();
            config.setConfFile(savedFile.getAbsolutePath());
            
            try {
                config.create();
                
                // Create graph from the configuration
                Graph graph = new Graph();
                graph.createFromTopics();
                
                // Generate dynamic HTML using HtmlGraphWriter
                List<String> htmlLines = HtmlGraphWriter.getGraphHTML(graph);
                
                // Send the dynamic HTML response
                sendDynamicGraphResponse(writer, htmlLines);
                
            } catch (Exception e) {
                sendErrorResponse(writer, "Error parsing configuration: " + e.getMessage());
            } finally {
                config.close();
            }
            
        } catch (Exception e) {
            sendErrorResponse(writer, "Error processing upload: " + e.getMessage());
        }
    }
    
    /**
     * Parses multipart form data to extract file information
     */
    private FileInfo parseMultipartData(String content) {
        // Look for Content-Disposition header with filename
        Pattern dispositionPattern = Pattern.compile("Content-Disposition: form-data; name=\"([^\"]+)\"(?:; filename=\"([^\"]+)\")?");
        
        String[] parts = content.split("------");
        
        for (String part : parts) {
            Matcher dispositionMatcher = dispositionPattern.matcher(part);
            if (dispositionMatcher.find()) {
                String fieldName = dispositionMatcher.group(1);
                String filename = dispositionMatcher.group(2);
                
                if ("configFile".equals(fieldName) && filename != null) {
                    // Find the actual file content (after double newline)
                    int contentStart = part.indexOf("\r\n\r\n");
                    if (contentStart != -1) {
                        contentStart += 4; // Skip the double newline
                        int contentEnd = part.lastIndexOf("\r\n");
                        if (contentEnd > contentStart) {
                            String fileContent = part.substring(contentStart, contentEnd);
                            return new FileInfo(filename, fileContent);
                        }
                    }
                }
            }
        }
        
        // Fallback: treat entire content as file content if multipart parsing fails
        if (!content.trim().isEmpty()) {
            return new FileInfo("uploaded_config.conf", content);
        }
        
        return null;
    }
    
    /**
     * Generates a unique filename to avoid conflicts
     */
    private String generateUniqueFilename(String originalFilename) {
        if (originalFilename == null || originalFilename.isEmpty()) {
            originalFilename = "config.conf";
        }
        
        // Add timestamp to make filename unique
        long timestamp = System.currentTimeMillis();
        String baseName = originalFilename;
        String extension = "";
        
        int lastDot = originalFilename.lastIndexOf('.');
        if (lastDot != -1) {
            baseName = originalFilename.substring(0, lastDot);
            extension = originalFilename.substring(lastDot);
        }
        
        return baseName + "_" + timestamp + extension;
    }
    
    /**
     * Saves file content to disk
     */
    private File saveFile(String filename, String content) throws IOException {
        File file = new File(UPLOAD_DIRECTORY, filename);
        
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(content.getBytes(StandardCharsets.UTF_8));
        }
        
        return file;
    }
    
    /**
     * Sends dynamic HTML response generated by HtmlGraphWriter
     */
    private void sendDynamicGraphResponse(PrintWriter writer, List<String> htmlLines) {
        writer.println("HTTP/1.1 200 OK");
        writer.println("Content-Type: text/html; charset=UTF-8");
        writer.println("Connection: close");
        writer.println();
        
        // Send each line of the generated HTML
        for (String line : htmlLines) {
            writer.println(line);
        }
    }
    
    /**
     * Sends error response
     */
    private void sendErrorResponse(PrintWriter writer, String errorMessage) {
        writer.println("HTTP/1.1 400 Bad Request");
        writer.println("Content-Type: text/html; charset=UTF-8");
        writer.println("Access-Control-Allow-Origin: *");
        writer.println("Access-Control-Allow-Methods: POST, GET, OPTIONS");
        writer.println("Access-Control-Allow-Headers: Content-Type");
        writer.println("Connection: close");
        writer.println();
        
        writer.println("<!DOCTYPE html>");
        writer.println("<html lang=\"en\">");
        writer.println("<head>");
        writer.println("    <meta charset=\"UTF-8\">");
        writer.println("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
        writer.println("    <title>Upload Error</title>");
        writer.println("    <style>");
        writer.println("        body {");
        writer.println("            font-family: Arial, sans-serif;");
        writer.println("            margin: 20px;");
        writer.println("            background-color: #f5f5f5;");
        writer.println("        }");
        writer.println("        .container {");
        writer.println("            background: white;");
        writer.println("            padding: 20px;");
        writer.println("            border-radius: 8px;");
        writer.println("            box-shadow: 0 2px 4px rgba(0,0,0,0.1);");
        writer.println("        }");
        writer.println("        h1 {");
        writer.println("            color: #dc3545;");
        writer.println("            margin-bottom: 20px;");
        writer.println("        }");
        writer.println("        .error {");
        writer.println("            color: #dc3545;");
        writer.println("            background-color: #f8d7da;");
        writer.println("            border: 1px solid #f5c6cb;");
        writer.println("            padding: 15px;");
        writer.println("            border-radius: 4px;");
        writer.println("        }");
        writer.println("    </style>");
        writer.println("</head>");
        writer.println("<body>");
        writer.println("    <div class=\"container\">");
        writer.println("        <h1>❌ Upload Failed</h1>");
        writer.println("        <div class=\"error\">");
        writer.println("            " + escapeHtml(errorMessage));
        writer.println("        </div>");
        writer.println("    </div>");
        writer.println("</body>");
        writer.println("</html>");
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
    
    /**
     * Helper class to hold file information
     */
    private static class FileInfo {
        final String filename;
        final String content;
        
        FileInfo(String filename, String content) {
            this.filename = filename;
            this.content = content;
        }
    }
}
