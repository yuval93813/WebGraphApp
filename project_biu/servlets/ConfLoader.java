package servlets;

import configs.GenericConfig;
import graph.Graph;
import graph.TopicManagerSingleton;
import views.HtmlGraphWriter;
import server.RequestParser.RequestInfo;

import java.io.*;

public class ConfLoader implements Servlet {
    @Override
    public void handle(RequestInfo ri, OutputStream toClient) throws Exception {
        // 1. Extract file name and contents from POST request
        String fileName = "uploaded.conf";
        byte[] fileContent = ri.getContent();
        
        // Debug: log the raw content received
        System.err.println("[DEBUG] Raw content length: " + fileContent.length);
        System.err.println("[DEBUG] Content preview: " + new String(fileContent, 0, Math.min(200, fileContent.length)));
        
        // Check if this is multipart form data
        String contentTypeHeader = null;
        for (String key : ri.getParameters().keySet()) {
            if (key.toLowerCase().equals("content-type")) {
                contentTypeHeader = ri.getParameters().get(key);
                break;
            }
        }
        
        if (contentTypeHeader != null && contentTypeHeader.contains("multipart/form-data")) {
            System.err.println("[DEBUG] Processing multipart form data");
            String body = new String(fileContent);
            
            // Extract boundary from content-type header
            String boundary = null;
            if (contentTypeHeader.contains("boundary=")) {
                boundary = contentTypeHeader.split("boundary=")[1].trim();
                System.err.println("[DEBUG] Boundary: " + boundary);
            }
            
            if (boundary != null) {
                // Split by boundary
                String[] parts = body.split("--" + boundary);
                System.err.println("[DEBUG] Found " + parts.length + " parts");
                
                for (int i = 0; i < parts.length; i++) {
                    String part = parts[i];
                    System.err.println("[DEBUG] Part " + i + " preview: " + part.substring(0, Math.min(100, part.length())));
                    
                    // Look for the file part (contains filename=)
                    if (part.contains("filename=") && part.contains("Content-Disposition: form-data")) {
                        // Extract filename
                        if (part.contains("filename=\"")) {
                            int fnStart = part.indexOf("filename=\"") + 10;
                            int fnEnd = part.indexOf("\"", fnStart);
                            if (fnEnd > fnStart) {
                                fileName = part.substring(fnStart, fnEnd);
                                System.err.println("[DEBUG] Extracted filename: " + fileName);
                            }
                        }
                        
                        // Extract file content (after double CRLF)
                        int contentStart = part.indexOf("\r\n\r\n");
                        if (contentStart != -1) {
                            contentStart += 4; // Skip the \r\n\r\n
                            // Find the end (before the next boundary or end)
                            String fileContentStr = part.substring(contentStart);
                            // Remove any trailing CRLF that might be part of the boundary
                            fileContentStr = fileContentStr.replaceAll("\\r?\\n$", "");
                            fileContent = fileContentStr.getBytes();
                            System.err.println("[DEBUG] Extracted file content length: " + fileContent.length);
                            System.err.println("[DEBUG] File content preview: " + new String(fileContent, 0, Math.min(100, fileContent.length)));
                            break;
                        }
                    }
                }
            }
        } else {
            System.err.println("[DEBUG] Processing as plain content");
        }
        File tempFile = File.createTempFile("conf_upload_", ".conf");
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(fileContent);
        }

        // 2. Create GenericConfig and Graph from file content
        // Clear previous topics/graph BEFORE creating new configuration
        TopicManagerSingleton.get().clear();
        System.err.println("[DEBUG] Cleared previous topics");
        
        GenericConfig config = new GenericConfig();
        config.setConfFile(tempFile.getAbsolutePath());
        System.err.println("[DEBUG] About to create config from file: " + tempFile.getAbsolutePath());
        config.create();
        System.err.println("[DEBUG] Config created successfully");
        
        Graph graph = new Graph();
        graph.createFromTopics();
        
        // Debug output
        System.err.println("[DEBUG] Graph created with " + graph.size() + " nodes");
        System.err.println("[DEBUG] Topics in TopicManager: " + TopicManagerSingleton.get().getTopics().size());

        // 3. Generate graphical HTML view of the computation graph
        StringBuilder html = new StringBuilder();
        html.append("<html><head><title>Computation Graph</title>");
        html.append("<style>");
        html.append("body{font-family:Arial,sans-serif;background:#f5f5f5;margin:0;padding:20px;}");
        html.append(".graph-container{background:white;border-radius:10px;box-shadow:0 4px 8px rgba(0,0,0,0.1);padding:20px;}");
        html.append(".graph-canvas{border:2px solid #ddd;border-radius:8px;background:#fafafa;width:100%;height:600px;position:relative;display:flex;justify-content:center;align-items:center;}");
        html.append("svg{width:800px;height:600px;max-width:100%;max-height:100%;}");
        html.append(".edge{stroke:#333;stroke-width:2;marker-end:url(#arrowhead);}");
        // Topic nodes (rectangles) - Blue theme
        html.append(".topic-node{fill:#4ECDC4;stroke:#26A69A;stroke-width:2;}");
        html.append(".topic-text{fill:white;font-weight:bold;font-size:12px;}");
        // Agent nodes (circles) - Green theme  
        html.append(".agent-node{fill:#FF6B6B;stroke:#E57373;stroke-width:2;}");
        html.append(".agent-text{fill:white;font-weight:bold;font-size:12px;}");
        // Message text
        html.append(".value-text{fill:#0066CC;font-weight:bold;font-size:10px;}");
        html.append(".result-text{fill:#CC6600;font-weight:bold;font-size:10px;}");
        html.append(".message-text{fill:#333;font-size:10px;font-weight:normal;}");
        html.append("a{display:inline-block;margin-top:15px;padding:10px 20px;background:#007bff;color:white;text-decoration:none;border-radius:4px;}");
        html.append("a:hover{background:#0056b3;}");
        html.append("</style>");
        html.append("</head><body>");
        html.append("<div class='graph-container'>");
        html.append("<h2>🔗 Computation Graph for: ").append(fileName).append("</h2>");
        html.append("<div class='graph-canvas'><svg viewBox='0 0 800 600' width='800' height='600'>");
        for (String svgLine : HtmlGraphWriter.getGraphSVG(graph)) {
            html.append(svgLine);
        }
        html.append("</svg></div>");


        // 4. Send HTTP response
        String response = "HTTP/1.1 200 OK\r\n" +
                "Content-Type: text/html; charset=UTF-8\r\n" +
                "Content-Length: " + html.length() + "\r\n" +
                "\r\n" +
                html.toString();
        toClient.write(response.getBytes("UTF-8"));
        toClient.flush();

        // Clean up temp file
        tempFile.delete();
    }

    @Override
    public void close() throws IOException {
        // Nothing to close
    }
}
