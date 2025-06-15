package servlets;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import configs.GenericConfig;
import graph.Graph;
import server.RequestParser.RequestInfo;

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
                
                // Send successful response with graph information
                sendSuccessResponse(writer, filename, fileInfo.content, graph);
                
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
        Pattern contentTypePattern = Pattern.compile("Content-Type: ([^\r\n]+)");
        
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
     * Sends successful response with graph information
     */
    private void sendSuccessResponse(PrintWriter writer, String filename, String content, Graph graph) {
        writer.println("HTTP/1.1 200 OK");
        writer.println("Content-Type: text/html; charset=UTF-8");
        writer.println("Connection: close");
        writer.println();
        
        writer.println("<!DOCTYPE html>");
        writer.println("<html lang=\"en\">");
        writer.println("<head>");
        writer.println("    <meta charset=\"UTF-8\">");
        writer.println("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
        writer.println("    <title>Configuration Deployed</title>");
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
        writer.println("            color: #28a745;");
        writer.println("            margin-bottom: 20px;");
        writer.println("        }");
        writer.println("        .info-box {");
        writer.println("            background: #f8f9fa;");
        writer.println("            border: 1px solid #dee2e6;");
        writer.println("            padding: 15px;");
        writer.println("            border-radius: 5px;");
        writer.println("            margin: 15px 0;");
        writer.println("        }");
        writer.println("        .config-content {");
        writer.println("            background: #f8f8f8;");
        writer.println("            border: 1px solid #ddd;");
        writer.println("            padding: 15px;");
        writer.println("            border-radius: 4px;");
        writer.println("            font-family: monospace;");
        writer.println("            white-space: pre-wrap;");
        writer.println("            max-height: 300px;");
        writer.println("            overflow-y: auto;");
        writer.println("        }");
        writer.println("        .graph-info {");
        writer.println("            display: grid;");
        writer.println("            grid-template-columns: 1fr 1fr;");
        writer.println("            gap: 15px;");
        writer.println("            margin-top: 20px;");
        writer.println("        }");
        writer.println("        .stat-box {");
        writer.println("            background: #e3f2fd;");
        writer.println("            border: 1px solid #90caf9;");
        writer.println("            padding: 15px;");
        writer.println("            border-radius: 5px;");
        writer.println("            text-align: center;");
        writer.println("        }");
        writer.println("        .stat-number {");
        writer.println("            font-size: 2em;");
        writer.println("            font-weight: bold;");
        writer.println("            color: #1976d2;");
        writer.println("        }");
        writer.println("        .success {");
        writer.println("            color: #28a745;");
        writer.println("            font-weight: bold;");
        writer.println("        }");
        writer.println("    </style>");
        writer.println("</head>");
        writer.println("<body>");
        writer.println("    <div class=\"container\">");
        writer.println("        <h1>🚀 Configuration Deployed Successfully</h1>");
        
        writer.println("        <div class=\"info-box\">");
        writer.println("            <h3>📁 File Information</h3>");
        writer.println("            <p><strong>Filename:</strong> " + escapeHtml(filename) + "</p>");
        writer.println("            <p><strong>Size:</strong> " + content.length() + " bytes</p>");
        writer.println("            <p><strong>Status:</strong> <span class=\"success\">Successfully Processed</span></p>");
        writer.println("        </div>");
        
        writer.println("        <div class=\"info-box\">");
        writer.println("            <h3>📋 Configuration Content</h3>");
        writer.println("            <div class=\"config-content\">" + escapeHtml(content) + "</div>");
        writer.println("        </div>");
        
        writer.println("        <div class=\"graph-info\">");
        writer.println("            <div class=\"stat-box\">");
        writer.println("                <div class=\"stat-number\">" + graph.size() + "</div>");
        writer.println("                <div>Graph Nodes</div>");
        writer.println("            </div>");
        writer.println("            <div class=\"stat-box\">");
        writer.println("                <div class=\"stat-number\">" + (graph.hasCycles() ? "Yes" : "No") + "</div>");
        writer.println("                <div>Has Cycles</div>");
        writer.println("            </div>");
        writer.println("        </div>");
        
        writer.println("        <div class=\"info-box\">");
        writer.println("            <h3>🔗 Graph Status</h3>");
        writer.println("            <p>Computation graph has been created and is ready for visualization.</p>");
        writer.println("            <p>The graph will be displayed in the visualization panel.</p>");
        writer.println("        </div>");
        
        writer.println("    </div>");
        writer.println("    <script>");
        writer.println("        // Notify parent frame that graph is ready");
        writer.println("        if (window.parent) {");
        writer.println("            window.parent.postMessage({action: 'showGraph'}, '*');");
        writer.println("        }");
        writer.println("    </script>");
        writer.println("</body>");
        writer.println("</html>");
    }
    
    /**
     * Sends error response
     */
    private void sendErrorResponse(PrintWriter writer, String errorMessage) {
        writer.println("HTTP/1.1 400 Bad Request");
        writer.println("Content-Type: text/html; charset=UTF-8");
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
