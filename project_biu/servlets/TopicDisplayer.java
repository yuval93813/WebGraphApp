package servlets;

import graph.Message;
import graph.Topic;
import graph.TopicManagerSingleton;
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
        
        // If both topic and message are provided, publish the message
        if (topicName != null && messageContent != null) {
            // Get or create the topic using TopicManager
            Topic topic = TopicManagerSingleton.get().getTopic(topicName);
            
            // Create a new message and publish it to the topic
            Message message = new Message(messageContent);
            topic.publish(message);
        }
        
        // Generate HTML response with a table of all topics and their last values
        generateHtmlResponse(toClient);
    }

    /**
     * Generates an HTML response containing a table with all topics and their last values.
     * The table has two columns: Topic Name and Last Value.
     */
    private void generateHtmlResponse(OutputStream toClient) throws IOException {
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
        writer.println("    </style>");
        writer.println("</head>");
        writer.println("<body>");
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
                    lastValue = "<em>No messages yet</em>";
                    status = "Empty";
                    statusClass = "empty";
                } else {
                    status = "Active";
                    statusClass = "active";
                    // Truncate long values for display
                    if (lastValue.length() > 20) {
                        lastValue = lastValue.substring(0, 17) + "...";
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