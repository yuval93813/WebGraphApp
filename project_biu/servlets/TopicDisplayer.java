package servlets;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Map;

import graph.Message;
import graph.Topic;
import graph.TopicManagerSingleton;
import server.RequestParser.RequestInfo;

/**
 * TopicDisplayer servlet that handles HTTP requests for publishing messages to topics.
 * This servlet processes requests to publish messages and returns an HTML page displaying
 * the current state of all topics.
 */
public class TopicDisplayer implements Servlet {

    /**
     * Handles HTTP requests for publishing messages to topics.
     * 
     * @param ri The request information containing HTTP details
     * @param toClient The output stream to send the response
     * @throws Exception If an error occurs during request processing
     */
    @Override
    public void handle(RequestInfo ri, OutputStream toClient) throws Exception {
        try {
            // Extract parameters from the request
            Map<String, String> parameters = ri.getParameters();
            String topicName = parameters.get("topic");
            String messageContent = parameters.get("message");

            // Validate parameters
            if (topicName != null && messageContent != null && !topicName.trim().isEmpty() && !messageContent.trim().isEmpty()) {
                // Decode URL-encoded parameters
                topicName = java.net.URLDecoder.decode(topicName, "UTF-8");
                messageContent = java.net.URLDecoder.decode(messageContent, "UTF-8");
                
                // Get the TopicManager singleton instance
                TopicManagerSingleton.TopicManager topicManager = TopicManagerSingleton.get();
                
                // Get or create the topic
                Topic topic = topicManager.getTopic(topicName);
                
                // Create and publish the message
                Message message = new Message(messageContent);
                topic.publish(message);
            }

            // Generate and send HTML response with all topics
            String htmlResponse = generateTopicsHtml();
            sendHttpResponse(toClient, htmlResponse);

        } catch (Exception e) {
            // Send error response
            String errorHtml = generateErrorHtml("Error processing request: " + e.getMessage());
            sendHttpResponse(toClient, errorHtml);
        }
    }

    /**
     * Generates an HTML page displaying all topics and their content.
     * 
     * @return HTML string containing a table of topics
     */
    private String generateTopicsHtml() {
        StringBuilder html = new StringBuilder();
        
        html.append("<!DOCTYPE html>\n");
        html.append("<html lang=\"en\">\n");
        html.append("<head>\n");
        html.append("    <meta charset=\"UTF-8\">\n");
        html.append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        html.append("    <title>Topic Status</title>\n");
        html.append("    <style>\n");
        html.append("        body {\n");
        html.append("            font-family: Arial, sans-serif;\n");
        html.append("            padding: 20px;\n");
        html.append("            background-color: #f9f9f9;\n");
        html.append("            margin: 0;\n");
        html.append("        }\n");
        html.append("        .container {\n");
        html.append("            background: white;\n");
        html.append("            border-radius: 8px;\n");
        html.append("            padding: 20px;\n");
        html.append("            box-shadow: 0 2px 4px rgba(0,0,0,0.1);\n");
        html.append("        }\n");
        html.append("        h1 {\n");
        html.append("            color: #333;\n");
        html.append("            margin-bottom: 20px;\n");
        html.append("            padding-bottom: 10px;\n");
        html.append("            border-bottom: 2px solid #007bff;\n");
        html.append("        }\n");
        html.append("        table {\n");
        html.append("            width: 100%;\n");
        html.append("            border-collapse: collapse;\n");
        html.append("            margin-top: 15px;\n");
        html.append("        }\n");
        html.append("        th, td {\n");
        html.append("            border: 1px solid #ddd;\n");
        html.append("            padding: 12px;\n");
        html.append("            text-align: left;\n");
        html.append("        }\n");
        html.append("        th {\n");
        html.append("            background-color: #f2f2f2;\n");
        html.append("            font-weight: bold;\n");
        html.append("        }\n");
        html.append("        tr:nth-child(even) {\n");
        html.append("            background-color: #f9f9f9;\n");
        html.append("        }\n");
        html.append("        .empty-message {\n");
        html.append("            text-align: center;\n");
        html.append("            color: #666;\n");
        html.append("            font-style: italic;\n");
        html.append("        }\n");
        html.append("        .back-button {\n");
        html.append("            margin-top: 20px;\n");
        html.append("            padding: 10px 20px;\n");
        html.append("            background-color: #007bff;\n");
        html.append("            color: white;\n");
        html.append("            text-decoration: none;\n");
        html.append("            border-radius: 4px;\n");
        html.append("            display: inline-block;\n");
        html.append("        }\n");
        html.append("        .back-button:hover {\n");
        html.append("            background-color: #0056b3;\n");
        html.append("        }\n");
        html.append("    </style>\n");
        html.append("</head>\n");
        html.append("<body>\n");
        html.append("    <div class=\"container\">\n");
        html.append("        <h1>📊 Topic Status</h1>\n");
        html.append("        <table>\n");
        html.append("            <thead>\n");
        html.append("                <tr>\n");
        html.append("                    <th>Topic Name</th>\n");
        html.append("                    <th>Content</th>\n");
        html.append("                </tr>\n");
        html.append("            </thead>\n");
        html.append("            <tbody>\n");

        // Get all topics from the TopicManager
        TopicManagerSingleton.TopicManager topicManager = TopicManagerSingleton.get();
        Collection<Topic> topics = topicManager.getTopics();

        if (topics.isEmpty()) {
            html.append("                <tr>\n");
            html.append("                    <td colspan=\"2\" class=\"empty-message\">No topics available</td>\n");
            html.append("                </tr>\n");
        } else {
            for (Topic topic : topics) {
                html.append("                <tr>\n");
                html.append("                    <td>").append(escapeHtml(topic.name)).append("</td>\n");
                html.append("                    <td>").append(escapeHtml(topic.getResult())).append("</td>\n");
                html.append("                </tr>\n");
            }
        }

        html.append("            </tbody>\n");
        html.append("        </table>\n");
        html.append("        <a href=\"/form.html\" class=\"back-button\">← Back to Forms</a>\n");
        html.append("    </div>\n");
        html.append("</body>\n");
        html.append("</html>");

        return html.toString();
    }

    /**
     * Generates an error HTML page.
     * 
     * @param errorMessage The error message to display
     * @return HTML string containing the error page
     */
    private String generateErrorHtml(String errorMessage) {
        StringBuilder html = new StringBuilder();
        
        html.append("<!DOCTYPE html>\n");
        html.append("<html lang=\"en\">\n");
        html.append("<head>\n");
        html.append("    <meta charset=\"UTF-8\">\n");
        html.append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        html.append("    <title>Error</title>\n");
        html.append("    <style>\n");
        html.append("        body { font-family: Arial, sans-serif; padding: 20px; background-color: #f9f9f9; }\n");
        html.append("        .container { background: white; border-radius: 8px; padding: 20px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }\n");
        html.append("        .error { color: #721c24; background-color: #f8d7da; border: 1px solid #f5c6cb; padding: 15px; border-radius: 4px; }\n");
        html.append("        .back-button { margin-top: 20px; padding: 10px 20px; background-color: #007bff; color: white; text-decoration: none; border-radius: 4px; display: inline-block; }\n");
        html.append("    </style>\n");
        html.append("</head>\n");
        html.append("<body>\n");
        html.append("    <div class=\"container\">\n");
        html.append("        <h1>❌ Error</h1>\n");
        html.append("        <div class=\"error\">").append(escapeHtml(errorMessage)).append("</div>\n");
        html.append("        <a href=\"/form.html\" class=\"back-button\">← Back to Forms</a>\n");
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
        
        // Send HTTP headers
        writer.println("HTTP/1.1 200 OK");
        writer.println("Content-Type: text/html; charset=UTF-8");
        writer.println("Content-Length: " + htmlContent.getBytes("UTF-8").length);
        writer.println(); // Empty line to separate headers from body
        
        // Send HTML content
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