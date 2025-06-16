package views;

import graph.Graph;
import graph.Node;
import graph.TopicManagerSingleton;
import graph.Topic;
import java.util.HashMap;
import java.util.Map;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * HtmlGraphWriter class responsible for generating HTML representations of Graph objects.
 * This class modifies the existing graph.html template with dynamic data instead of 
 * creating completely new HTML from scratch.
 */
public class HtmlGraphWriter {
    
    // Path to the template graph.html file
    private static final String TEMPLATE_PATH = "html_files/graph.html";
    
    /**
     * Generates an HTML page by modifying the existing graph.html template with 
     * dynamic graph data.
     * 
     * @param graph The Graph object to visualize
     * @return A complete HTML string with the graph data injected
     */
    public static String getGraphHTML(Graph graph) {
        if (graph == null) {
            return generateErrorHTML("Graph is null");
        }
        
        try {
            // Try multiple possible paths for the template file
            String templateHtml = null;
            String[] possiblePaths = {
                TEMPLATE_PATH,
                "../" + TEMPLATE_PATH,
                "../../" + TEMPLATE_PATH,
                System.getProperty("user.dir") + "/" + TEMPLATE_PATH,
                System.getProperty("user.dir") + "/../" + TEMPLATE_PATH
            };
            
            for (String path : possiblePaths) {
                try {
                    templateHtml = new String(Files.readAllBytes(Paths.get(path)));
                    break; // Success, stop trying other paths
                } catch (IOException e) {
                    // Try next path
                    continue;
                }
            }
            
            if (templateHtml == null) {
                return generateErrorHTML("Could not find graph template file. Working directory: " + System.getProperty("user.dir"));
            }
            
            // Generate the dynamic graph data
            String dynamicGraphFunction = generateDynamicGraphFunction(graph);
            
            // Replace the static example function with our dynamic one
            String modifiedHtml = replaceGraphFunction(templateHtml, dynamicGraphFunction);
            
            // Update the title and statistics
            modifiedHtml = updateGraphInfo(modifiedHtml, graph);
            
            // Update the initialization to use our dynamic function
            modifiedHtml = updateInitialization(modifiedHtml);
            
            return modifiedHtml;
            
        } catch (Exception e) {
            return generateErrorHTML("Error generating graph: " + e.getMessage());
        }
    }
    
    /**
     * Generates the JavaScript function that creates the dynamic graph from the Graph object.
     */
    private static String generateDynamicGraphFunction(Graph graph) {
        StringBuilder function = new StringBuilder();
        
        function.append("        // Generate system graph from configuration data\n");
        function.append("        function generateSystemGraph() {\n");
        function.append("            clearGraph();\n");
        function.append("            \n");
        
        if (graph.isEmpty()) {
            function.append("            // No nodes to display\n");
            function.append("            const infoDiv = document.getElementById('graphInfo');\n");
            function.append("            infoDiv.innerHTML = '<p>No graph data available. The graph is empty.</p>';\n");
            function.append("            return;\n");
        } else {
            // Calculate node positions
            Map<String, NodePosition> positions = calculateNodePositions(graph);
            
            // Create JavaScript nodes
            function.append("            // Create nodes from system configuration\n");
            for (Node node : graph) {
                NodePosition pos = positions.get(node.getName());
                String nodeType = node.getName().startsWith("T") ? "operator" : "input"; // Topics as operators, Agents as inputs
                String escapedLabel = escapeJavaScript(node.getName());
                
                // Get topic value if this is a topic
                String topicValue = "null";
                if (node.getName().startsWith("T")) {
                    try {
                        Topic topic = TopicManagerSingleton.get().getTopic(node.getName());
                        String value = topic.getResult();
                        if (value != null && !value.isEmpty()) {
                            topicValue = "'" + escapeJavaScript(value) + "'";
                        } else {
                            topicValue = "''"; // Empty string for empty topics
                        }
                    } catch (Exception e) {
                        topicValue = "null"; // Default if topic not found
                    }
                }
                
                function.append("            const node").append(nodeId(node.getName())).append(" = new GraphNode('").append(escapedLabel).append("', '").append(nodeType).append("', ").append(pos.x).append(", ").append(pos.y).append(", ").append(topicValue).append(");\n");
            }
            
            function.append("            \n");
            function.append("            // Add nodes to array\n");
            function.append("            nodes = [");
            boolean first = true;
            for (Node node : graph) {
                if (!first) function.append(", ");
                function.append("node").append(nodeId(node.getName()));
                first = false;
            }
            function.append("];\n");
            function.append("            \n");
            function.append("            // Create node elements\n");
            function.append("            nodes.forEach(node => createNodeElement(node));\n");
            function.append("            \n");
            function.append("            // Define edges\n");
            function.append("            edges = [\n");
            
            boolean firstEdge = true;
            for (Node fromNode : graph) {
                for (Node toNode : fromNode.getEdges()) {
                    if (!firstEdge) function.append(",\n");
                    function.append("                new GraphEdge(node").append(nodeId(fromNode.getName())).append(", node").append(nodeId(toNode.getName())).append(")");
                    firstEdge = false;
                }
            }
            
            function.append("\n            ];\n");
            function.append("            \n");
            function.append("            // Draw arrows\n");
            function.append("            edges.forEach(edge => drawArrow(edge.from, edge.to));\n");
            function.append("            \n");
            function.append("            // Update info\n");
            function.append("            updateGraphInfo(null, 'System Graph loaded from configuration.\\nShowing actual communication topology.');\n");
        }
        
        function.append("        }\n");
        
        return function.toString();
    }
    
    /**
     * Replaces the generateExampleGraph function with our dynamic function.
     */
    private static String replaceGraphFunction(String html, String newFunction) {
        // Find the start and end of the generateExampleGraph function
        String startPattern = "        // Generate example graph: (A + B) * (A - B)";
        
        int startIndex = html.indexOf(startPattern);
        if (startIndex == -1) {
            // Fallback: look for the function declaration
            startPattern = "        function generateExampleGraph() {";
            startIndex = html.indexOf(startPattern);
        }
        
        if (startIndex != -1) {
            // Find the matching closing brace
            int braceCount = 0;
            int searchIndex = startIndex;
            int endIndex = -1;
            
            // Skip to the opening brace
            while (searchIndex < html.length() && html.charAt(searchIndex) != '{') {
                searchIndex++;
            }
            
            if (searchIndex < html.length()) {
                braceCount = 1;
                searchIndex++; // Move past the opening brace
                
                while (searchIndex < html.length() && braceCount > 0) {
                    char c = html.charAt(searchIndex);
                    if (c == '{') {
                        braceCount++;
                    } else if (c == '}') {
                        braceCount--;
                    }
                    searchIndex++;
                }
                
                if (braceCount == 0) {
                    endIndex = searchIndex;
                }
            }
            
            if (endIndex != -1) {
                return html.substring(0, startIndex) + newFunction + html.substring(endIndex);
            }
        }
        
        // If we couldn't find the function, just append our function before the complex graph function
        String insertPoint = "        // Generate a more complex example";
        int insertIndex = html.indexOf(insertPoint);
        if (insertIndex != -1) {
            return html.substring(0, insertIndex) + newFunction + "\n        " + html.substring(insertIndex);
        }
        
        // Last resort: append before the script closing tag
        insertPoint = "    </script>";
        insertIndex = html.lastIndexOf(insertPoint);
        if (insertIndex != -1) {
            return html.substring(0, insertIndex) + "        " + newFunction + "\n    " + html.substring(insertIndex);
        }
        
        return html; // Return unchanged if we can't find where to insert
    }
    
    /**
     * Updates the graph title and node type legend.
     */
    private static String updateGraphInfo(String html, Graph graph) {
        // Update the title
        html = html.replace("🔗 Computation Graph Visualization", "🔗 System Communication Graph");
        
        // Count different node types
        int topicCount = 0;
        int agentCount = 0;
        
        for (Node node : graph) {
            if (node.getName().startsWith("T")) {
                topicCount++;
            } else if (node.getName().startsWith("A")) {
                agentCount++;
            }
        }
        
        // Update the legend
        String oldLegend = "                    <li><span style=\"color: #4ECDC4; font-weight: bold;\">■</span> Input Variables (A, B, C, etc.)</li>\n" +
                          "                    <li><span style=\"color: #FF6B6B; font-weight: bold;\">■</span> Operators (+, -, *, /, etc.)</li>\n" +
                          "                    <li><span style=\"color: #45B7D1; font-weight: bold;\">■</span> Output Results</li>\n" +
                          "                    <li><span style=\"color: #4CAF50; font-weight: bold;\">■</span> Intermediate Results</li>";
        
        String newLegend = "                    <li><span style=\"color: #4ECDC4; font-weight: bold;\">■</span> Agents (" + agentCount + " total)</li>\n" +
                          "                    <li><span style=\"color: #FF6B6B; font-weight: bold;\">■</span> Topics (" + topicCount + " total)</li>\n" +
                          "                    <li><span style=\"color: #45B7D1; font-weight: bold;\">■</span> Communication Flow</li>\n" +
                          "                    <li><span style=\"color: #4CAF50; font-weight: bold;\">■</span> Total Nodes: " + graph.size() + "</li>";
        
        return html.replace(oldLegend, newLegend);
    }
    
    /**
     * Updates the initialization to call our dynamic function instead of the example.
     */
    private static String updateInitialization(String html) {
        // Replace the initialization to call our function
        html = html.replace("generateExampleGraph();", "generateSystemGraph();");
        html = html.replace("loadLiveGraphData();", "generateSystemGraph();");
        
        return html;
    }
    
    // ... existing helper methods (calculateNodePositions, NodePosition, escapeJavaScript, etc.) ...
    
    /**
     * Simple class to hold node position coordinates.
     */
    private static class NodePosition {
        final int x;
        final int y;
        
        NodePosition(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
    
    /**
     * Calculates positions for nodes using a flow-based layout that represents the data flow structure.
     */
    private static Map<String, NodePosition> calculateNodePositions(Graph graph) {
        Map<String, NodePosition> positions = new HashMap<>();
        
        // Separate nodes by type and analyze structure
        java.util.List<Node> topics = new java.util.ArrayList<>();
        java.util.List<Node> agents = new java.util.ArrayList<>();
        
        for (Node node : graph) {
            if (node.getName().startsWith("T")) {
                topics.add(node);
            } else if (node.getName().startsWith("A")) {
                agents.add(node);
            }
        }
        
        // Layout parameters
        int spacing = 180;
        int verticalSpacing = 120;
        int startX = 60;
        int startY = 60;
        
        // Find input topics (topics with no incoming edges from agents)
        java.util.List<Node> inputTopics = new java.util.ArrayList<>();
        java.util.List<Node> outputTopics = new java.util.ArrayList<>();
        java.util.List<Node> intermediateTopics = new java.util.ArrayList<>();
        
        for (Node topic : topics) {
            boolean hasAgentInput = false;
            boolean hasAgentOutput = false;
            
            // Check if any agent publishes to this topic
            for (Node agent : agents) {
                for (Node edge : agent.getEdges()) {
                    if (edge.equals(topic)) {
                        hasAgentInput = true;
                        break;
                    }
                }
            }
            
            // Check if this topic publishes to any agent
            for (Node edge : topic.getEdges()) {
                if (edge.getName().startsWith("A")) {
                    hasAgentOutput = true;
                    break;
                }
            }
            
            if (!hasAgentInput) {
                inputTopics.add(topic);
            } else if (!hasAgentOutput) {
                outputTopics.add(topic);
            } else {
                intermediateTopics.add(topic);
            }
        }
        
        // Position input topics in the leftmost column
        int currentY = startY;
        for (int i = 0; i < inputTopics.size(); i++) {
            Node topic = inputTopics.get(i);
            positions.put(topic.getName(), new NodePosition(startX, currentY));
            currentY += verticalSpacing;
        }
        
        // Position agents that consume from input topics
        int agentX = startX + spacing;
        currentY = startY;
        java.util.Set<Node> processedAgents = new java.util.HashSet<>();
        
        for (Node inputTopic : inputTopics) {
            // Find agents that subscribe to this input topic
            java.util.List<Node> subscribingAgents = new java.util.ArrayList<>();
            for (Node edge : inputTopic.getEdges()) {
                if (edge.getName().startsWith("A") && !processedAgents.contains(edge)) {
                    subscribingAgents.add(edge);
                }
            }
            
            // If multiple agents subscribe to the same input topic, position them vertically
            for (Node agent : subscribingAgents) {
                positions.put(agent.getName(), new NodePosition(agentX, currentY));
                processedAgents.add(agent);
                currentY += verticalSpacing;
            }
        }
        
        // Position intermediate topics (outputs of agents)
        int intermediateX = agentX + spacing;
        currentY = startY;
        for (Node agent : processedAgents) {
            for (Node edge : agent.getEdges()) {
                if (edge.getName().startsWith("T") && !positions.containsKey(edge.getName())) {
                    positions.put(edge.getName(), new NodePosition(intermediateX, currentY));
                    currentY += verticalSpacing;
                }
            }
        }
        
        // Position remaining agents (those that process intermediate topics)
        int finalAgentX = intermediateX + spacing;
        currentY = startY;
        for (Node agent : agents) {
            if (!processedAgents.contains(agent)) {
                positions.put(agent.getName(), new NodePosition(finalAgentX, currentY));
                currentY += verticalSpacing;
            }
        }
        
        // Position output topics in the rightmost column
        int outputX = finalAgentX + spacing;
        currentY = startY;
        for (Node outputTopic : outputTopics) {
            if (!positions.containsKey(outputTopic.getName())) {
                positions.put(outputTopic.getName(), new NodePosition(outputX, currentY));
                currentY += verticalSpacing;
            }
        }
        
        // Handle any remaining unpositioned nodes
        currentY = startY + (Math.max(inputTopics.size(), agents.size()) * verticalSpacing);
        for (Node node : graph) {
            if (!positions.containsKey(node.getName())) {
                positions.put(node.getName(), new NodePosition(startX, currentY));
                currentY += verticalSpacing;
            }
        }
        
        return positions;
    }
    
    /**
     * Generates a valid JavaScript identifier from a node name.
     */
    private static String nodeId(String nodeName) {
        return nodeName.replaceAll("[^a-zA-Z0-9]", "_");
    }
    
    /**
     * Escapes special characters for JavaScript strings.
     */
    private static String escapeJavaScript(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\")
                   .replace("'", "\\'")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }
    
    /**
     * Generates an error HTML page when the graph cannot be displayed.
     */
    private static String generateErrorHTML(String errorMessage) {
        StringBuilder html = new StringBuilder();
        
        html.append("<!DOCTYPE html>\n");
        html.append("<html lang=\"en\">\n");
        html.append("<head>\n");
        html.append("    <meta charset=\"UTF-8\">\n");
        html.append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        html.append("    <title>Graph Visualization Error</title>\n");
        html.append("    <style>\n");
        html.append("        body { font-family: Arial, sans-serif; padding: 20px; background-color: #f9f9f9; text-align: center; }\n");
        html.append("        .container { background: white; border-radius: 8px; padding: 40px; margin: 50px auto; max-width: 600px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }\n");
        html.append("        .error { color: #721c24; background-color: #f8d7da; border: 1px solid #f5c6cb; padding: 15px; border-radius: 4px; margin: 20px 0; }\n");
        html.append("        .back-button { margin-top: 20px; padding: 10px 20px; background-color: #007bff; color: white; text-decoration: none; border-radius: 4px; display: inline-block; }\n");
        html.append("    </style>\n");
        html.append("</head>\n");
        html.append("<body>\n");
        html.append("    <div class=\"container\">\n");
        html.append("        <h1>❌ Graph Visualization Error</h1>\n");
        html.append("        <div class=\"error\">").append(escapeHtml(errorMessage)).append("</div>\n");
        html.append("        <a href=\"/form.html\" class=\"back-button\">← Back to Forms</a>\n");
        html.append("    </div>\n");
        html.append("</body>\n");
        html.append("</html>");
        
        return html.toString();
    }
    
    /**
     * Escapes HTML special characters to prevent XSS attacks.
     */
    private static String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;")
                  .replace("'", "&#39;");
    }
}