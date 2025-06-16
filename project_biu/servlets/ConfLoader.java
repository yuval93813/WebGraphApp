package servlets;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.File;
import java.io.FileWriter;

import server.RequestParser.RequestInfo;
import configs.GenericConfig;
import graph.Graph;
import views.HtmlGraphWriter;

/**
 * ConfLoader servlet that handles configuration file uploads.
 * This servlet processes POST requests containing configuration files.
 */
public class ConfLoader implements Servlet {

    /**
     * Handles HTTP requests for configuration file uploads.
     * After loading the configuration, creates a graph and returns HTML visualization.
     * 
     * @param ri The request information containing HTTP details
     * @param toClient The output stream to send the response
     * @throws Exception If an error occurs during request processing
     */
    @Override
    public void handle(RequestInfo ri, OutputStream toClient) throws Exception {
        try {
            // Get the uploaded content
            byte[] content = ri.getContent();
            
            if (content != null && content.length > 0) {
                // Process the configuration file
                String configContent = new String(content, "UTF-8");
                
                // Create a temporary file to store the configuration
                File tempConfigFile = File.createTempFile("uploaded_config", ".conf");
                try (FileWriter writer = new FileWriter(tempConfigFile)) {
                    writer.write(configContent);
                }
                
                // Load and execute the configuration
                GenericConfig config = new GenericConfig();
                config.setConfFile(tempConfigFile.getAbsolutePath());
                
                try {
                    // Create agents from configuration
                    config.create();
                    
                    // Generate graph from current topics
                    Graph graph = new Graph();
                    graph.createFromTopics();
                    
                    // Generate HTML visualization
                    String graphHtml = HtmlGraphWriter.getGraphHTML(graph);
                    sendHttpResponse(toClient, graphHtml);
                    
                    // Clean up - close the config after a brief delay to allow graph generation
                    Thread.sleep(100);
                    config.close();
                    
                } catch (Exception e) {
                    // If configuration loading fails, show error
                    String errorResponse = generateErrorHtml("Error processing configuration: " + e.getMessage());
                    sendHttpResponse(toClient, errorResponse);
                }
                
                // Clean up temporary file
                tempConfigFile.delete();
                
            } else {
                String errorResponse = generateErrorHtml("No configuration file content received");
                sendHttpResponse(toClient, errorResponse);
            }
            
        } catch (Exception e) {
            String errorResponse = generateErrorHtml("Error processing configuration: " + e.getMessage());
            sendHttpResponse(toClient, errorResponse);
        }
    }

    /**
     * Generates an error HTML page.
     * 
     * @param errorMessage The error message to display
     * @return HTML string for the error page
     */
    private String generateErrorHtml(String errorMessage) {
        StringBuilder html = new StringBuilder();
        
        html.append("<!DOCTYPE html>\n");
        html.append("<html lang=\"en\">\n");
        html.append("<head>\n");
        html.append("    <meta charset=\"UTF-8\">\n");
        html.append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        html.append("    <title>Upload Error</title>\n");
        html.append("    <style>\n");
        html.append("        body { font-family: Arial, sans-serif; padding: 20px; background-color: #f9f9f9; text-align: center; }\n");
        html.append("        .container { background: white; border-radius: 8px; padding: 40px; margin: 50px auto; max-width: 600px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }\n");
        html.append("        .error { color: #721c24; background-color: #f8d7da; border: 1px solid #f5c6cb; padding: 15px; border-radius: 4px; margin: 20px 0; }\n");
        html.append("        .back-button { margin-top: 20px; padding: 10px 20px; background-color: #007bff; color: white; text-decoration: none; border-radius: 4px; display: inline-block; }\n");
        html.append("    </style>\n");
        html.append("</head>\n");
        html.append("<body>\n");
        html.append("    <div class=\"container\">\n");
        html.append("        <h1>❌ Upload Failed</h1>\n");
        html.append("        <div class=\"error\">").append(escapeHtml(errorMessage)).append("</div>\n");
        html.append("        <a href=\"/app/form.html\" class=\"back-button\">← Back to Forms</a>\n");
        html.append("    </div>\n");
        html.append("</body>\n");
        html.append("</html>");

        return html.toString();
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
     * Sends an HTTP response with the given HTML content.
     * 
     * @param toClient The output stream to send the response
     * @param htmlContent The HTML content to send
     * @throws IOException If an I/O error occurs
     */
    private void sendHttpResponse(OutputStream toClient, String htmlContent) throws IOException {
        PrintWriter writer = new PrintWriter(toClient, true);
        
        writer.println("HTTP/1.1 200 OK");
        writer.println("Content-Type: text/html; charset=UTF-8");
        writer.println("Content-Length: " + htmlContent.getBytes("UTF-8").length);
        writer.println(); // Empty line to separate headers from body
        
        writer.print(htmlContent);
        writer.flush();
    }

    /**
     * Closes the servlet and releases any resources.
     */
    @Override
    public void close() throws IOException {
        // No resources to close in this implementation
    }
}