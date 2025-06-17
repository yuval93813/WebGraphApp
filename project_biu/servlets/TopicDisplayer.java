package servlets;

import graph.Message;
import graph.Topic;
import graph.TopicManagerSingleton;
import graph.Graph;
import graph.Node;
import server.RequestParser.RequestInfo;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Map;

/**
 * TopicDisplayer servlet handles HTTP requests to publish messages to topics
 * and displays all topics with their last values in an HTML table format.
 * 
 * This servlet:
 * 1. Extracts topic and message parameters from the HTTP request
 * 2. Uses TopicManager to publish the message to the specified topic
 * 3. Returns a 2-column HTML table showing all topics and their last values
 */
public class TopicDisplayer implements Servlet {

    @Override
    public void handle(RequestInfo ri, OutputStream toClient) throws Exception {
        // Extract topic and message from HTTP request parameters
        Map<String, String> parameters = ri.getParameters();
        String topicName = parameters.get("topic");
        String messageContent = parameters.get("message");
        
        String errorMessage = null;
        String successMessage = null;
        
        // If both topic and message are provided, validate and publish the message
        if (topicName != null && messageContent != null) {
            // Validate topic name
            if (!isValidTopicName(topicName)) {
                errorMessage = "Invalid topic name: " + topicName + ". Topic name must be alphanumeric and cannot be empty.";
            } else if (!topicExists(topicName)) {
                errorMessage = "Topic " + topicName + " does not exist. Please check available topics at the system status frame at the right frame.";
            } else {
                // Get the existing topic using TopicManager
                Topic topic = TopicManagerSingleton.get().getTopic(topicName);
                
                // Create a new message and publish it to the topic
                Message message = new Message(messageContent);
                topic.publish(message);
                
                // Set success message
                successMessage = "Message " + messageContent + " successfully published to topic " + topicName + ".";
            }
        }
        
        // Generate HTML response with a table of all topics and their last values
        generateHtmlResponse(toClient, errorMessage, successMessage);
    }

    /**
     * Validates if a topic name is valid (not null, not empty, alphanumeric).
     */
    private boolean isValidTopicName(String topicName) {
        if (topicName == null || topicName.trim().isEmpty()) {
            return false;
        }
        // Check if topic name contains only alphanumeric characters and underscores
        return topicName.matches("^[a-zA-Z0-9_]+$");
    }
    
    /**
     * Checks if a topic exists in the TopicManager.
     */
    private boolean topicExists(String topicName) {
        Collection<Topic> topics = TopicManagerSingleton.get().getTopics();
        for (Topic topic : topics) {
            if (topic.name.equals(topicName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Generates an HTML section with graph information including node count, topic count, and cycle detection.
     */
    private void generateGraphInfoSection(PrintWriter writer) throws IOException {
        // Create a graph from current topics to analyze
        Graph graph = new Graph();
        graph.createFromTopics();
        
        // Count nodes by type
        int topicNodes = 0;
        int agentNodes = 0;
        for (Node node : graph) {
            if (node.getName().startsWith("T")) {
                topicNodes++;
            } else if (node.getName().startsWith("A")) {
                agentNodes++;
            }
        }
        int totalNodes = topicNodes + agentNodes;
        
        // Check for cycles
        boolean hasCycles = graph.hasCycles();
        String cycleStatus = hasCycles ? "⚠️ Detected" : "✅ None";
        String cycleClass = hasCycles ? "cycle-warning" : "cycle-ok";
        
        // Generate the graph info section
        writer.println("    <h3>🔗 Graph Information</h3>");
        writer.println("    <div class=\"graph-info\">");
        writer.println("        <div class=\"info-item\">");
        writer.println("            <span class=\"info-label\">Total Nodes:</span>");
        writer.println("            <span class=\"info-value\">" + totalNodes + "</span>");
        writer.println("        </div>");
        writer.println("        <div class=\"info-item\">");
        writer.println("            <span class=\"info-label\">Topic Nodes:</span>");
        writer.println("            <span class=\"info-value topic-count\">" + topicNodes + "</span>");
        writer.println("        </div>");
        writer.println("        <div class=\"info-item\">");
        writer.println("            <span class=\"info-label\">Agent Nodes:</span>");
        writer.println("            <span class=\"info-value agent-count\">" + agentNodes + "</span>");
        writer.println("        </div>");
        writer.println("        <div class=\"info-item\">");
        writer.println("            <span class=\"info-label\">Cycles:</span>");
        writer.println("            <span class=\"info-value " + cycleClass + "\">" + cycleStatus + "</span>");
        writer.println("        </div>");
        writer.println("    </div>");
        writer.println("    <hr style=\"margin: 15px 0; border: none; border-top: 1px solid #ddd;\">");
    }

    /**
     * Generates an HTML response containing a table with all topics and their last values.
     * The table has two columns: Topic Name and Last Value.
     */
    private void generateHtmlResponse(OutputStream toClient, String errorMessage, String successMessage) throws IOException {
        PrintWriter writer = new PrintWriter(toClient);
        
        // Send HTTP headers
        writer.println("HTTP/1.1 200 OK");
        writer.println("Content-Type: text/html; charset=UTF-8");
        writer.println("Connection: close");
        writer.println(); // Empty line to separate headers from body
        
        // Start HTML document
        writer.println("<!DOCTYPE html>");
        writer.println("<html>");
        writer.println("<head>");
        writer.println("    <title>Topic Status</title>");
        writer.println("    <style>");
        writer.println("        body { font-family: Arial, sans-serif; margin: 10px; font-size: 12px; }");
        writer.println("        h3 { color: #333; margin: 0 0 10px 0; font-size: 14px; }");
        writer.println("        table { border-collapse: collapse; width: 100%; margin-top: 10px; }");
        writer.println("        th, td { border: 1px solid #ddd; padding: 6px; text-align: left; font-size: 11px; }");
        writer.println("        th { background-color: #f2f2f2; font-weight: bold; }");
        writer.println("        tr:nth-child(even) { background-color: #f9f9f9; }");
        writer.println("        tr:hover { background-color: #f5f5f5; }");
        writer.println("        .no-data { text-align: center; color: #666; font-style: italic; }");
        writer.println("        .topic-name { font-weight: bold; color: #007bff; }");
        writer.println("        .last-value { color: #333; word-break: break-word; }");
        writer.println("        .status-badge { display: inline-block; padding: 2px 6px; border-radius: 3px; font-size: 10px; }");
        writer.println("        .active { background-color: #d4edda; color: #155724; }");
        writer.println("        .empty { background-color: #f8d7da; color: #721c24; }");
        writer.println("        .graph-info { margin-bottom: 15px; }");
        writer.println("        .info-item { display: flex; justify-content: space-between; padding: 4px 0; }");
        writer.println("        .info-label { font-weight: bold; color: #333; }");
        writer.println("        .info-value { color: #666; }");
        writer.println("        .topic-count { color: #007bff; font-weight: bold; }");
        writer.println("        .agent-count { color: #dc3545; font-weight: bold; }");
        writer.println("        .cycle-ok { color: #28a745; font-weight: bold; }");
        writer.println("        .cycle-warning { color: #dc3545; font-weight: bold; }");
        writer.println("        .error-message { background-color: #f8d7da; color: #721c24; border: 1px solid #f5c6cb; padding: 10px; border-radius: 4px; margin-bottom: 15px; transition: opacity 0.5s ease-out; }");
        writer.println("        .success-message { background-color: #d4edda; color: #155724; border: 1px solid #c3e6cb; padding: 10px; border-radius: 4px; margin-bottom: 15px; transition: opacity 0.5s ease-out; }");
        writer.println("        .fade-out { opacity: 0; }");
        writer.println("    </style>");
        writer.println("</head>");
        writer.println("<body>");
        
        // Display error message if present
        if (errorMessage != null) {
            writer.println("    <div class=\"error-message\">");
            writer.println("        <strong>❌ Error:</strong> " + escapeHtml(errorMessage));
            writer.println("    </div>");
        }
        
        // Display success message if present
        if (successMessage != null) {
            writer.println("    <div class=\"success-message\">");
            writer.println("        <strong>✅ Success:</strong> " + escapeHtml(successMessage));
            writer.println("    </div>");
        }
        
        // Graph Information Section
        generateGraphInfoSection(writer);
        
        writer.println("    <h3>📊 Topics Status</h3>");
        
        // Create the table
        writer.println("    <table>");
        writer.println("        <thead>");
        writer.println("            <tr>");
        writer.println("                <th>Topic</th>");
        writer.println("                <th>Last Value</th>");
        writer.println("                <th>Status</th>");
        writer.println("            </tr>");
        writer.println("        </thead>");
        writer.println("        <tbody>");
        
        // Get all topics from TopicManager
        Collection<Topic> topics = TopicManagerSingleton.get().getTopics();
        
        if (topics.isEmpty()) {
            // Display message when no topics exist
            writer.println("            <tr>");
            writer.println("                <td colspan=\"3\" class=\"no-data\">No topics available</td>");
            writer.println("            </tr>");
        } else {
            // Display each topic and its last value
            for (Topic topic : topics) {
                String lastValue = topic.getResult();
                String status;
                String statusClass;
                
                // Determine status and styling
                if (lastValue == null || lastValue.isEmpty()) {
                    lastValue = "No messages yet";
                    status = "Empty";
                    statusClass = "empty";
                } else {
                    status = "Active";
                    statusClass = "active";
                    // Truncate long values for display
                    if (lastValue.length() > 20) {
                        lastValue = lastValue.substring(0, 20) + "...";
                    }
                }
                
                writer.println("            <tr>");
                writer.println("                <td class=\"topic-name\">" + escapeHtml(topic.name) + "</td>");
                writer.println("                <td class=\"last-value\">" + escapeHtml(lastValue) + "</td>");
                writer.println("                <td><span class=\"status-badge " + statusClass + "\">" + status + "</span></td>");
                writer.println("            </tr>");
            }
        }
        
        writer.println("        </tbody>");
        writer.println("    </table>");
        
        // Add JavaScript to auto-hide messages
        writer.println("    <script>");
        writer.println("        // Auto-hide error and success messages after 5 seconds");
        writer.println("        setTimeout(function() {");
        writer.println("            const errorMessage = document.querySelector('.error-message');");
        writer.println("            const successMessage = document.querySelector('.success-message');");
        writer.println("            ");
        writer.println("            if (errorMessage) {");
        writer.println("                errorMessage.classList.add('fade-out');");
        writer.println("                setTimeout(function() {");
        writer.println("                    errorMessage.style.display = 'none';");
        writer.println("                }, 500); // Wait for fade transition to complete");
        writer.println("            }");
        writer.println("            ");
        writer.println("            if (successMessage) {");
        writer.println("                successMessage.classList.add('fade-out');");
        writer.println("                setTimeout(function() {");
        writer.println("                    successMessage.style.display = 'none';");
        writer.println("                }, 500); // Wait for fade transition to complete");
        writer.println("            }");
        writer.println("        }, 4000); // Start fading after 4 seconds");
        writer.println("    </script>");
        
        writer.println("</body>");
        writer.println("</html>");
        
        writer.flush();
    }
    
    /**
     * Escapes HTML special characters to prevent XSS attacks and ensure proper display.
     */
    private String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#x27;");
    }

    @Override
    public void close() throws IOException {
        // No resources to clean up for this servlet
    }
}