package views;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import graph.Graph;
import graph.Node;
import graph.TopicManagerSingleton;
import graph.Topic;

/**
 * HtmlGraphWriter provides functionality to generate dynamic HTML content
 * for visualizing computation graphs. This class follows the separation of
 * concerns principle by using template-based HTML generation.
 */
public class HtmlGraphWriter {
    
    private static final String TEMPLATE_PATH = "views/graph_template.html";
    private static final String GRAPH_DATA_PLACEHOLDER = "<!-- GRAPH_DATA -->";
    private static final String GRAPH_STATS_PLACEHOLDER = "<!-- GRAPH_STATS -->";
    
    /**
     * Generates a list of HTML lines that render a graphical representation
     * of the given computation graph.
     * 
     * @param graph The Graph object to visualize
     * @return List of HTML strings representing the complete HTML page
     */
    public static List<String> getGraphHTML(Graph graph) {
        try {
            // Load the HTML template
            List<String> templateLines = loadTemplate();
            
            // Generate graph data as JavaScript
            String graphDataJS = generateGraphDataJS(graph);
            
            // Generate graph statistics
            String graphStatsHTML = generateGraphStatsHTML(graph);
            
            // Replace placeholders with actual content
            List<String> finalHTML = new ArrayList<>();
            
            for (String line : templateLines) {
                if (line.contains(GRAPH_DATA_PLACEHOLDER)) {
                    finalHTML.add(line.replace(GRAPH_DATA_PLACEHOLDER, graphDataJS));
                } else if (line.contains(GRAPH_STATS_PLACEHOLDER)) {
                    finalHTML.add(line.replace(GRAPH_STATS_PLACEHOLDER, graphStatsHTML));
                } else {
                    finalHTML.add(line);
                }
            }
            
            return finalHTML;
            
        } catch (Exception e) {
            // Fallback: return basic error HTML
            return generateErrorHTML("Error generating graph visualization: " + e.getMessage());
        }
    }
    
    /**
     * Loads the HTML template from the file system or classpath
     */
    private static List<String> loadTemplate() throws IOException {
        List<String> lines = new ArrayList<>();
        
        try {
            // First try to load from file system (relative to working directory)
            try (BufferedReader reader = new BufferedReader(new FileReader(TEMPLATE_PATH))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    lines.add(line);
                }
            }
        } catch (IOException e) {
            // Fallback: try to load from classpath
            try (InputStream is = HtmlGraphWriter.class.getClassLoader().getResourceAsStream(TEMPLATE_PATH);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                
                if (is != null) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        lines.add(line);
                    }
                } else {
                    throw new IOException("Template file not found: " + TEMPLATE_PATH);
                }
            }
        }
        
        return lines;
    }
    
    /**
     * Generates JavaScript code containing the graph data
     */
    private static String generateGraphDataJS(Graph graph) {
        StringBuilder js = new StringBuilder();
        
        js.append("<script>\n");
        js.append("// Dynamically generated graph data\n");
        js.append("const graphData = {\n");
        
        // Generate nodes data
        js.append("  nodes: [\n");
        Map<Node, Integer> nodeIdMap = new HashMap<>();
        int nodeIndex = 0;
        
        for (Node node : graph) {
            nodeIdMap.put(node, nodeIndex);
            
            String nodeType = determineNodeType(node);
            String nodeLabel = escapeForJS(node.getName());
            
            js.append(String.format("    { id: %d, label: '%s', type: '%s' }",
                nodeIndex, nodeLabel, nodeType));
            
            if (nodeIndex < graph.size() - 1) {
                js.append(",");
            }
            js.append("\n");
            nodeIndex++;
        }
        js.append("  ],\n");
        
        // Generate edges data
        js.append("  edges: [\n");
        List<String> edges = new ArrayList<>();
        
        for (Node node : graph) {
            int fromId = nodeIdMap.get(node);
            for (Node neighbor : node.getEdges()) {
                if (nodeIdMap.containsKey(neighbor)) {
                    int toId = nodeIdMap.get(neighbor);
                    edges.add(String.format("    { from: %d, to: %d }", fromId, toId));
                }
            }
        }
        
        for (int i = 0; i < edges.size(); i++) {
            js.append(edges.get(i));
            if (i < edges.size() - 1) {
                js.append(",");
            }
            js.append("\n");
        }
        
        js.append("  ]\n");
        js.append("};\n");
        
        // Add topic information if available
        js.append("const topicData = [\n");
        for (Topic topic : TopicManagerSingleton.get().getTopics()) {
            js.append(String.format("  { name: '%s', subscribers: %d, publishers: %d, lastMessage: '%s' },\n",
                escapeForJS(topic.name),
                topic.getSubscribers().size(),
                topic.getPublishers().size(),
                escapeForJS(topic.getResult())));
        }
        js.append("];\n");
        
        js.append("// Initialize graph visualization\n");
        js.append("if (typeof initializeGraphVisualization === 'function') {\n");
        js.append("  initializeGraphVisualization(graphData, topicData);\n");
        js.append("}\n");
        js.append("</script>");
        
        return js.toString();
    }
    
    /**
     * Generates HTML content for graph statistics
     */
    private static String generateGraphStatsHTML(Graph graph) {
        StringBuilder html = new StringBuilder();
        
        html.append("<div class=\"graph-statistics\">\n");
        html.append("  <h3>📊 Graph Statistics</h3>\n");
        html.append("  <div class=\"stats-grid\">\n");
        
        // Node count
        html.append("    <div class=\"stat-item\">\n");
        html.append("      <div class=\"stat-value\">").append(graph.size()).append("</div>\n");
        html.append("      <div class=\"stat-label\">Total Nodes</div>\n");
        html.append("    </div>\n");
        
        // Edge count
        int totalEdges = 0;
        for (Node node : graph) {
            totalEdges += node.getEdges().size();
        }
        html.append("    <div class=\"stat-item\">\n");
        html.append("      <div class=\"stat-value\">").append(totalEdges).append("</div>\n");
        html.append("      <div class=\"stat-label\">Total Edges</div>\n");
        html.append("    </div>\n");
        
        // Cycle detection
        html.append("    <div class=\"stat-item\">\n");
        html.append("      <div class=\"stat-value\" style=\"color: ")
            .append(graph.hasCycles() ? "#dc3545" : "#28a745").append("\">");
        html.append(graph.hasCycles() ? "Yes" : "No").append("</div>\n");
        html.append("      <div class=\"stat-label\">Has Cycles</div>\n");
        html.append("    </div>\n");
        
        // Topic count
        int topicCount = TopicManagerSingleton.get().getTopics().size();
        html.append("    <div class=\"stat-item\">\n");
        html.append("      <div class=\"stat-value\">").append(topicCount).append("</div>\n");
        html.append("      <div class=\"stat-label\">Active Topics</div>\n");
        html.append("    </div>\n");
        
        html.append("  </div>\n");
        html.append("</div>\n");
        
        return html.toString();
    }
    
    /**
     * Determines the type of a node for visualization purposes
     */
    private static String determineNodeType(Node node) {
        String name = node.getName().toLowerCase();
        
        if (name.startsWith("t")) {
            return "topic";
        } else if (name.startsWith("a")) {
            return "agent";
        } else if (name.contains("input") || name.matches(".*[a-z]$")) {
            return "input";
        } else if (name.contains("output") || name.contains("result")) {
            return "output";
        } else if (name.matches(".*[+\\-*/÷×].*")) {
            return "operator";
        } else {
            return "intermediate";
        }
    }
    
    /**
     * Escapes strings for safe inclusion in JavaScript code
     */
    private static String escapeForJS(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                  .replace("'", "\\'")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
    
    /**
     * Generates a basic error HTML page as fallback
     */
    private static List<String> generateErrorHTML(String errorMessage) {
        List<String> html = new ArrayList<>();
        html.add("<!DOCTYPE html>");
        html.add("<html lang=\"en\">");
        html.add("<head>");
        html.add("    <meta charset=\"UTF-8\">");
        html.add("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
        html.add("    <title>Graph Visualization Error</title>");
        html.add("    <style>");
        html.add("        body { font-family: Arial, sans-serif; margin: 40px; background: #f5f5f5; }");
        html.add("        .error-container { background: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }");
        html.add("        .error-title { color: #dc3545; font-size: 24px; margin-bottom: 20px; }");
        html.add("        .error-message { color: #666; line-height: 1.6; }");
        html.add("    </style>");
        html.add("</head>");
        html.add("<body>");
        html.add("    <div class=\"error-container\">");
        html.add("        <div class=\"error-title\">⚠️ Visualization Error</div>");
        html.add("        <div class=\"error-message\">" + escapeForHTML(errorMessage) + "</div>");
        html.add("    </div>");
        html.add("</body>");
        html.add("</html>");
        return html;
    }
    
    /**
     * Escapes HTML special characters
     */
    private static String escapeForHTML(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;")
                  .replace("'", "&#x27;");
    }
}
