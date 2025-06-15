package servlets;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import graph.Message;
import graph.Topic;
import graph.TopicManagerSingleton;
import server.RequestParser.RequestInfo;

/**
 * TopicDisplayer servlet handles GET requests to /publish endpoint.
 * It extracts topic and message parameters from the query string,
 * publishes the message using the TopicManager singleton,
 * and returns an HTML table displaying the topic and last published message.
 */
public class TopicDisplayer implements Servlet {

    @Override
    public void handle(RequestInfo ri, OutputStream toClient) throws Exception {
        PrintWriter writer = new PrintWriter(toClient, true);
        
        try {
            // Extract parameters from the request
            String topicName = ri.getParameters().get("topic");
            String messageText = ri.getParameters().get("message");
            
            // URL decode the parameters
            if (topicName != null) {
                topicName = URLDecoder.decode(topicName, StandardCharsets.UTF_8.toString());
            }
            if (messageText != null) {
                messageText = URLDecoder.decode(messageText, StandardCharsets.UTF_8.toString());
            }
            
            // Validate parameters
            if (topicName == null || topicName.trim().isEmpty()) {
                sendErrorResponse(writer, "Missing or empty 'topic' parameter");
                return;
            }
            
            if (messageText == null || messageText.trim().isEmpty()) {
                sendErrorResponse(writer, "Missing or empty 'message' parameter");
                return;
            }
            
            // Get or create the topic using TopicManager singleton
            Topic topic = TopicManagerSingleton.get().getTopic(topicName);
            
            // Create and publish the message
            Message message = new Message(messageText);
            topic.publish(message);
            
            // Send successful response with HTML table
            sendSuccessResponse(writer, topicName, messageText, topic.getResult());
            
        } catch (Exception e) {
            sendErrorResponse(writer, "Error processing request: " + e.getMessage());
        }
    }
    
    /**
     * Sends a successful HTML response with a table displaying topic information
     */
    private void sendSuccessResponse(PrintWriter writer, String topicName, String messageText, String lastMessage) {
        writer.println("HTTP/1.1 200 OK");
        writer.println("Content-Type: text/html; charset=UTF-8");
        writer.println("Connection: close");
        writer.println();
        
        writer.println("<!DOCTYPE html>");
        writer.println("<html lang=\"en\">");
        writer.println("<head>");
        writer.println("    <meta charset=\"UTF-8\">");
        writer.println("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
        writer.println("    <title>Topic Message Published</title>");
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
        writer.println("        table {");
        writer.println("            width: 100%;");
        writer.println("            border-collapse: collapse;");
        writer.println("            margin-top: 15px;");
        writer.println("        }");
        writer.println("        th, td {");
        writer.println("            border: 1px solid #ddd;");
        writer.println("            padding: 12px;");
        writer.println("            text-align: left;");
        writer.println("        }");
        writer.println("        th {");
        writer.println("            background-color: #f2f2f2;");
        writer.println("            font-weight: bold;");
        writer.println("        }");
        writer.println("        tr:nth-child(even) {");
        writer.println("            background-color: #f9f9f9;");
        writer.println("        }");
        writer.println("        .success {");
        writer.println("            color: #28a745;");
        writer.println("            font-weight: bold;");
        writer.println("        }");
        writer.println("    </style>");
        writer.println("</head>");
        writer.println("<body>");
        writer.println("    <div class=\"container\">");
        writer.println("        <h1>✅ Message Published Successfully</h1>");
        writer.println("        <table>");
        writer.println("            <thead>");
        writer.println("                <tr>");
        writer.println("                    <th>Property</th>");
        writer.println("                    <th>Value</th>");
        writer.println("                </tr>");
        writer.println("            </thead>");
        writer.println("            <tbody>");
        writer.println("                <tr>");
        writer.println("                    <td><strong>Topic Name</strong></td>");
        writer.println("                    <td>" + escapeHtml(topicName) + "</td>");
        writer.println("                </tr>");
        writer.println("                <tr>");
        writer.println("                    <td><strong>Published Message</strong></td>");
        writer.println("                    <td>" + escapeHtml(messageText) + "</td>");
        writer.println("                </tr>");
        writer.println("                <tr>");
        writer.println("                    <td><strong>Last Message in Topic</strong></td>");
        writer.println("                    <td>" + escapeHtml(lastMessage) + "</td>");
        writer.println("                </tr>");
        writer.println("                <tr>");
        writer.println("                    <td><strong>Status</strong></td>");
        writer.println("                    <td class=\"success\">Published</td>");
        writer.println("                </tr>");
        writer.println("            </tbody>");
        writer.println("        </table>");
        writer.println("    </div>");
        writer.println("</body>");
        writer.println("</html>");
    }
    
    /**
     * Sends an error response with appropriate HTTP status and HTML content
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
        writer.println("    <title>Error - Topic Publisher</title>");
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
        writer.println("        <h1>❌ Error Publishing Message</h1>");
        writer.println("        <div class=\"error\">");
        writer.println("            " + escapeHtml(errorMessage));
        writer.println("        </div>");
        writer.println("    </div>");
        writer.println("</body>");
        writer.println("</html>");
    }
    
    /**
     * Escapes HTML special characters to prevent XSS attacks
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
