package servlets;

import graph.*;
import views.HtmlGraphWriter;
import server.RequestParser.RequestInfo;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

/**
 * GraphUpdateServlet generates real-time visual graph updates that reflect current topic values and agent states.
 * 
 * This servlet:
 * 1. Creates a graph from current topics and agents
 * 2. Updates graph nodes with current topic values and agent results
 * 3. Returns an HTML page with SVG visualization showing real-time data
 * 4. Makes the graph nodes visually update when topic messages are published
 */
public class GraphUpdateServlet implements Servlet {

    @Override
    public void handle(RequestInfo ri, OutputStream toClient) throws Exception {
        // Create graph from current topics
        Graph graph = new Graph();
        graph.createFromTopics();
        
        // Update nodes with current topic values and agent states
        updateNodesWithCurrentValues(graph);
        
        // Generate HTML response with updated graph visualization
        generateGraphHtmlResponse(toClient, graph);
    }

    /**
     * Updates graph nodes with current topic values and calculated agent results.
     * This ensures the visual graph reflects the real-time state of the system.
     */
    private void updateNodesWithCurrentValues(Graph graph) {
        for (Node node : graph) {
            String nodeName = node.getName();
            
            if (nodeName.startsWith("T")) {
                // Topic node - update with current topic value
                String topicName = nodeName.substring(1); // Remove "T" prefix
                Topic topic = TopicManagerSingleton.get().getTopic(topicName);
                if (topic != null && topic.getResult() != null && !topic.getResult().isEmpty()) {
                    // Create a simple message that displays the actual value
                    Message valueMessage = new Message(topic.getResult());
                    node.setMsg(valueMessage);
                }
            } else if (nodeName.startsWith("A")) {
                // Agent node - update with calculated result if available
                String agentName = nodeName.substring(1); // Remove "A" prefix
                updateAgentNodeWithResult(node, agentName);
            }
        }
    }

    /**
     * Updates an agent node with its calculated result by checking output topics.
     * This allows the graph to show agent computation results visually.
     */
    private void updateAgentNodeWithResult(Node node, String agentName) {
        // Find agents with this name and check their output topics
        for (Topic topic : TopicManagerSingleton.get().getTopics()) {
            for (Agent publisher : topic.getPublishers()) {
                if (publisher.getName().equals(agentName)) {
                    // This agent publishes to this topic - use topic's result as agent's output
                    String result = topic.getResult();
                    if (result != null && !result.isEmpty()) {
                        // Create a message that shows the result value properly
                        Message resultMessage = new Message("→ " + result);
                        node.setMsg(resultMessage);
                        return;
                    }
                }
            }
        }
    }

    /**
     * Generates an HTML response containing the updated graph visualization.
     * The response includes SVG graphics showing current topic values and agent results.
     */
    private void generateGraphHtmlResponse(OutputStream toClient, Graph graph) throws IOException {
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
        writer.println("    <title>Real-time Computation Graph</title>");
        writer.println("    <style>");
        writer.println("        body { font-family: Arial, sans-serif; background: #f5f5f5; margin: 0; padding: 20px; }");
        writer.println("        .graph-container { background: white; border-radius: 10px; box-shadow: 0 4px 8px rgba(0,0,0,0.1); padding: 20px; }");
        writer.println("        .graph-title { text-align: center; color: #333; margin-bottom: 20px; font-size: 24px; font-weight: bold; }");
        writer.println("        .graph-canvas { border: 2px solid #ddd; border-radius: 8px; background: #fafafa; width: 100%; height: 600px; position: relative; display: flex; justify-content: center; align-items: center; }");
        writer.println("        svg { width: 800px; height: 600px; max-width: 100%; max-height: 100%; }");
        writer.println("        .edge { stroke: #333; stroke-width: 2; marker-end: url(#arrowhead); }");
        
        // Topic nodes (rectangles) - Blue theme
        writer.println("        .topic-node { fill: #4ECDC4; stroke: #26A69A; stroke-width: 2; }");
        writer.println("        .topic-text { fill: white; font-weight: bold; font-size: 12px; }");
        
        // Agent nodes (circles) - Red theme  
        writer.println("        .agent-node { fill: #FF6B6B; stroke: #E57373; stroke-width: 2; }");
        writer.println("        .agent-text { fill: white; font-weight: bold; font-size: 12px; }");
        
        // Value and result text
        writer.println("        .value-text { fill: #0066CC; font-weight: bold; font-size: 11px; }");
        writer.println("        .result-text { fill: #CC6600; font-weight: bold; font-size: 11px; }");
        writer.println("        .message-text { fill: #333; font-size: 10px; font-weight: normal; }");
        
        // Info panel
        writer.println("        .info-panel { margin-top: 20px; background: #f8f9fa; padding: 15px; border-radius: 8px; border: 1px solid #dee2e6; }");
        writer.println("        .btn { background: #007bff; color: white; border: none; padding: 8px 16px; border-radius: 4px; cursor: pointer; margin: 0 5px; font-size: 14px; text-decoration: none; display: inline-block; }");
        writer.println("        .btn:hover { background: #0056b3; }");
        writer.println("    </style>");
        writer.println("</head>");
        writer.println("<body>");
        writer.println("    <div class='graph-container'>");
        writer.println("        <div class='graph-title'>🔗 Real-time Computation Graph</div>");
        

        
        // Graph canvas with SVG
        writer.println("        <div class='graph-canvas'>");
        writer.println("            <svg viewBox='0 0 800 600' width='800' height='600'>");
        
        // Generate SVG content for the graph
        for (String svgLine : HtmlGraphWriter.getGraphSVG(graph)) {
            writer.println("                " + svgLine);
        }
        
        writer.println("            </svg>");
        writer.println("        </div>");
        
        // Info panel
        writer.println("        <div class='info-panel'>");
        writer.println("            <h4>📊 Graph Information</h4>");
        writer.println("            <div id='graphInfo'>");
        writer.println("                <p><strong>Nodes:</strong> " + graph.size() + "</p>");
        writer.println("                <p><strong>Topics:</strong> " + TopicManagerSingleton.get().getTopics().size() + "</p>");
        writer.println("                <p><strong>Has Cycles:</strong> " + (graph.hasCycles() ? "Yes" : "No") + "</p>");
        writer.println("                <p><strong>Status:</strong> Real-time visualization showing current topic values and agent results</p>");
        
        // Display topic values
        if (!TopicManagerSingleton.get().getTopics().isEmpty()) {
            writer.println("                <p><strong>Current Topic Values:</strong></p>");
            writer.println("                <ul>");
            for (Topic topic : TopicManagerSingleton.get().getTopics()) {
                String value = topic.getResult();
                if (value == null || value.isEmpty()) {
                    value = "<em>No value</em>";
                }
                writer.println("                    <li><strong>" + escapeHtml(topic.name) + ":</strong> " + escapeHtml(value) + "</li>");
            }
            writer.println("                </ul>");
        }
        
        writer.println("            </div>");
        writer.println("        </div>");
        writer.println("    </div>");
        
        // JavaScript - no auto-refresh, graph updates only when needed
        writer.println("    <script>");
        writer.println("        // Graph is updated only when configuration is uploaded or messages are sent");
        writer.println("        console.log('Real-time computation graph loaded');");
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
