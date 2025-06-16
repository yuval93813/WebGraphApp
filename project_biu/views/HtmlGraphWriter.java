package views;

import graph.Graph;
import graph.Node;
import java.util.HashMap;
import java.util.Map;

/**
 * HtmlGraphWriter class responsible for generating HTML representations of Graph objects.
 * This class follows the separation of concerns principle by separating data visualization 
 * from business logic.
 */
public class HtmlGraphWriter {
    
    /**
     * Generates a complete HTML page that visualizes the given Graph object.
     * The HTML includes CSS styling and JavaScript for interactive visualization.
     * 
     * @param graph The Graph object to visualize
     * @return A complete HTML string representing the graph visualization
     */
    public static String getGraphHTML(Graph graph) {
        if (graph == null) {
            return generateErrorHTML("Graph is null");
        }
        
        StringBuilder html = new StringBuilder();
        
        // Generate the complete HTML structure
        html.append(generateHTMLHeader());
        html.append(generateBodyStart());
        html.append(generateGraphContainer(graph));
        html.append(generateJavaScript(graph));
        html.append(generateHTMLFooter());
        
        return html.toString();
    }
    
    /**
     * Generates the HTML header with CSS styling.
     */
    private static String generateHTMLHeader() {
        StringBuilder header = new StringBuilder();
        
        header.append("<!DOCTYPE html>\n");
        header.append("<html lang=\"en\">\n");
        header.append("<head>\n");
        header.append("    <meta charset=\"UTF-8\">\n");
        header.append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        header.append("    <title>System Graph Visualization</title>\n");
        header.append("    <style>\n");
        header.append(generateCSS());
        header.append("    </style>\n");
        header.append("</head>\n");
        
        return header.toString();
    }
    
    /**
     * Generates the CSS styling for the graph visualization.
     */
    private static String generateCSS() {
        StringBuilder css = new StringBuilder();
        
        css.append("        body {\n");
        css.append("            font-family: Arial, sans-serif;\n");
        css.append("            margin: 0;\n");
        css.append("            padding: 20px;\n");
        css.append("            background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);\n");
        css.append("            overflow: auto;\n");
        css.append("        }\n");
        
        css.append("        .graph-container {\n");
        css.append("            background: white;\n");
        css.append("            border-radius: 10px;\n");
        css.append("            box-shadow: 0 4px 8px rgba(0,0,0,0.1);\n");
        css.append("            padding: 20px;\n");
        css.append("            min-height: calc(100vh - 80px);\n");
        css.append("        }\n");
        
        css.append("        .graph-title {\n");
        css.append("            text-align: center;\n");
        css.append("            color: #333;\n");
        css.append("            margin-bottom: 20px;\n");
        css.append("            font-size: 24px;\n");
        css.append("            font-weight: bold;\n");
        css.append("        }\n");
        
        css.append("        .graph-canvas {\n");
        css.append("            border: 2px solid #ddd;\n");
        css.append("            border-radius: 8px;\n");
        css.append("            background: #fafafa;\n");
        css.append("            width: 100%;\n");
        css.append("            height: 600px;\n");
        css.append("            position: relative;\n");
        css.append("            overflow: auto;\n");
        css.append("        }\n");
        
        css.append("        .node {\n");
        css.append("            position: absolute;\n");
        css.append("            background: #4CAF50;\n");
        css.append("            color: white;\n");
        css.append("            padding: 10px 15px;\n");
        css.append("            border-radius: 8px;\n");
        css.append("            box-shadow: 0 2px 4px rgba(0,0,0,0.2);\n");
        css.append("            font-weight: bold;\n");
        css.append("            font-size: 14px;\n");
        css.append("            text-align: center;\n");
        css.append("            cursor: pointer;\n");
        css.append("            transition: all 0.3s ease;\n");
        css.append("            border: 2px solid #45a049;\n");
        css.append("        }\n");
        
        css.append("        .node:hover {\n");
        css.append("            transform: scale(1.05);\n");
        css.append("            box-shadow: 0 4px 8px rgba(0,0,0,0.3);\n");
        css.append("        }\n");
        
        css.append("        .node.topic {\n");
        css.append("            background: #FF6B6B;\n");
        css.append("            border-color: #e55353;\n");
        css.append("        }\n");
        
        css.append("        .node.agent {\n");
        css.append("            background: #4ECDC4;\n");
        css.append("            border-color: #3bb5ad;\n");
        css.append("        }\n");
        
        css.append("        .arrow-line {\n");
        css.append("            stroke: #666;\n");
        css.append("            stroke-width: 2;\n");
        css.append("            marker-end: url(#arrowhead);\n");
        css.append("        }\n");
        
        css.append("        .info-panel {\n");
        css.append("            background: #f8f9fa;\n");
        css.append("            border: 1px solid #dee2e6;\n");
        css.append("            border-radius: 5px;\n");
        css.append("            padding: 15px;\n");
        css.append("            margin-top: 20px;\n");
        css.append("        }\n");
        
        css.append("        .back-button {\n");
        css.append("            margin-top: 20px;\n");
        css.append("            padding: 10px 20px;\n");
        css.append("            background-color: #007bff;\n");
        css.append("            color: white;\n");
        css.append("            text-decoration: none;\n");
        css.append("            border-radius: 4px;\n");
        css.append("            display: inline-block;\n");
        css.append("        }\n");
        
        css.append("        .back-button:hover {\n");
        css.append("            background-color: #0056b3;\n");
        css.append("        }\n");
        
        return css.toString();
    }
    
    /**
     * Generates the opening body tag.
     */
    private static String generateBodyStart() {
        return "<body>\n";
    }
    
    /**
     * Generates the main graph container with dynamic content based on the Graph object.
     */
    private static String generateGraphContainer(Graph graph) {
        StringBuilder container = new StringBuilder();
        
        container.append("    <div class=\"graph-container\">\n");
        container.append("        <div class=\"graph-title\">🔗 System Communication Graph</div>\n");
        container.append("        \n");
        container.append("        <div class=\"graph-canvas\" id=\"graphCanvas\">\n");
        container.append("            <svg width=\"100%\" height=\"100%\" style=\"position: absolute; top: 0; left: 0;\">\n");
        container.append("                <defs>\n");
        container.append("                    <marker id=\"arrowhead\" markerWidth=\"10\" markerHeight=\"7\" \n");
        container.append("                            refX=\"9\" refY=\"3.5\" orient=\"auto\">\n");
        container.append("                        <polygon points=\"0 0, 10 3.5, 0 7\" fill=\"#666\" />\n");
        container.append("                    </marker>\n");
        container.append("                </defs>\n");
        container.append("            </svg>\n");
        container.append("        </div>\n");
        container.append("        \n");
        container.append("        <div class=\"info-panel\">\n");
        container.append("            <h4>📋 Graph Information</h4>\n");
        container.append("            <div id=\"graphInfo\">\n");
        container.append("                <p><strong>Graph Statistics:</strong></p>\n");
        container.append("                <p>Total Nodes: ").append(graph.size()).append("</p>\n");
        
        // Count different node types
        int topicCount = 0;
        int agentCount = 0;
        int totalEdges = 0;
        
        for (Node node : graph) {
            totalEdges += node.getEdges().size();
            if (node.getName().startsWith("T")) {
                topicCount++;
            } else if (node.getName().startsWith("A")) {
                agentCount++;
            }
        }
        
        container.append("                <p>Topics: ").append(topicCount).append("</p>\n");
        container.append("                <p>Agents: ").append(agentCount).append("</p>\n");
        container.append("                <p>Total Edges: ").append(totalEdges).append("</p>\n");
        container.append("                <p><strong>Node Types:</strong></p>\n");
        container.append("                <ul>\n");
        container.append("                    <li><span style=\"color: #FF6B6B; font-weight: bold;\">■</span> Topics (T prefix)</li>\n");
        container.append("                    <li><span style=\"color: #4ECDC4; font-weight: bold;\">■</span> Agents (A prefix)</li>\n");
        container.append("                </ul>\n");
        container.append("                <p>Click on a node to see its details.</p>\n");
        container.append("            </div>\n");
        container.append("        </div>\n");
        container.append("        \n");
        container.append("        <a href=\"/form.html\" class=\"back-button\">← Back to Forms</a>\n");
        container.append("    </div>\n");
        
        return container.toString();
    }
    
    /**
     * Generates JavaScript code for interactive graph visualization.
     */
    private static String generateJavaScript(Graph graph) {
        StringBuilder js = new StringBuilder();
        
        js.append("    <script>\n");
        js.append("        // Graph data from server\n");
        js.append("        let nodes = [];\n");
        js.append("        let edges = [];\n");
        js.append("        let nodeId = 0;\n");
        js.append("        \n");
        js.append("        // Node class\n");
        js.append("        class GraphNode {\n");
        js.append("            constructor(label, type, x, y) {\n");
        js.append("                this.id = ++nodeId;\n");
        js.append("                this.label = label;\n");
        js.append("                this.type = type;\n");
        js.append("                this.x = x;\n");
        js.append("                this.y = y;\n");
        js.append("                this.element = null;\n");
        js.append("            }\n");
        js.append("        }\n");
        js.append("        \n");
        js.append("        // Edge class\n");
        js.append("        class GraphEdge {\n");
        js.append("            constructor(fromNode, toNode) {\n");
        js.append("                this.from = fromNode;\n");
        js.append("                this.to = toNode;\n");
        js.append("            }\n");
        js.append("        }\n");
        js.append("        \n");
        
        // Generate node positioning and creation logic
        js.append(generateNodeCreationJS(graph));
        
        js.append("        \n");
        js.append("        // Create a node in the DOM\n");
        js.append("        function createNodeElement(node) {\n");
        js.append("            const canvas = document.getElementById('graphCanvas');\n");
        js.append("            const nodeElement = document.createElement('div');\n");
        js.append("            nodeElement.className = `node ${node.type}`;\n");
        js.append("            nodeElement.textContent = node.label;\n");
        js.append("            nodeElement.style.left = `${node.x}px`;\n");
        js.append("            nodeElement.style.top = `${node.y}px`;\n");
        js.append("            nodeElement.title = `${node.type}: ${node.label}`;\n");
        js.append("            \n");
        js.append("            nodeElement.addEventListener('click', () => {\n");
        js.append("                updateGraphInfo(node);\n");
        js.append("            });\n");
        js.append("            \n");
        js.append("            canvas.appendChild(nodeElement);\n");
        js.append("            node.element = nodeElement;\n");
        js.append("            return nodeElement;\n");
        js.append("        }\n");
        js.append("        \n");
        js.append("        // Draw an arrow between two nodes\n");
        js.append("        function drawArrow(fromNode, toNode) {\n");
        js.append("            const svg = document.querySelector('#graphCanvas svg');\n");
        js.append("            const line = document.createElementNS('http://www.w3.org/2000/svg', 'line');\n");
        js.append("            \n");
        js.append("            const fromX = fromNode.x + 40;\n");
        js.append("            const fromY = fromNode.y + 20;\n");
        js.append("            const toX = toNode.x + 40;\n");
        js.append("            const toY = toNode.y + 20;\n");
        js.append("            \n");
        js.append("            line.setAttribute('x1', fromX);\n");
        js.append("            line.setAttribute('y1', fromY);\n");
        js.append("            line.setAttribute('x2', toX);\n");
        js.append("            line.setAttribute('y2', toY);\n");
        js.append("            line.setAttribute('class', 'arrow-line');\n");
        js.append("            \n");
        js.append("            svg.appendChild(line);\n");
        js.append("        }\n");
        js.append("        \n");
        js.append("        // Update graph information panel\n");
        js.append("        function updateGraphInfo(selectedNode) {\n");
        js.append("            const infoDiv = document.getElementById('graphInfo');\n");
        js.append("            \n");
        js.append("            if (selectedNode) {\n");
        js.append("                infoDiv.innerHTML = `\n");
        js.append("                    <p><strong>Selected Node:</strong> ${selectedNode.label}</p>\n");
        js.append("                    <p><strong>Type:</strong> ${selectedNode.type}</p>\n");
        js.append("                    <p><strong>Position:</strong> (${selectedNode.x}, ${selectedNode.y})</p>\n");
        js.append("                    <p><strong>ID:</strong> ${selectedNode.id}</p>\n");
        js.append("                `;\n");
        js.append("            }\n");
        js.append("        }\n");
        js.append("        \n");
        js.append("        // Initialize the graph when page loads\n");
        js.append("        window.addEventListener('load', () => {\n");
        js.append("            generateGraph();\n");
        js.append("        });\n");
        js.append("    </script>\n");
        
        return js.toString();
    }
    
    /**
     * Generates JavaScript code for creating nodes and edges from the Graph object.
     */
    private static String generateNodeCreationJS(Graph graph) {
        StringBuilder js = new StringBuilder();
        
        js.append("        // Generate graph from server data\n");
        js.append("        function generateGraph() {\n");
        
        if (graph.isEmpty()) {
            js.append("            // No nodes to display\n");
            js.append("            const infoDiv = document.getElementById('graphInfo');\n");
            js.append("            infoDiv.innerHTML = '<p>No graph data available. The graph is empty.</p>';\n");
            js.append("            return;\n");
        } else {
            // Calculate node positions using a simple layout algorithm
            Map<String, NodePosition> positions = calculateNodePositions(graph);
            
            // Create JavaScript nodes
            js.append("            // Create nodes\n");
            for (Node node : graph) {
                NodePosition pos = positions.get(node.getName());
                String nodeType = node.getName().startsWith("T") ? "topic" : "agent";
                String escapedLabel = escapeJavaScript(node.getName());
                
                js.append("            const node").append(nodeId(node.getName())).append(" = new GraphNode('").append(escapedLabel).append("', '").append(nodeType).append("', ").append(pos.x).append(", ").append(pos.y).append(");\n");
            }
            
            js.append("            \n");
            js.append("            // Add nodes to array\n");
            js.append("            nodes = [");
            boolean first = true;
            for (Node node : graph) {
                if (!first) js.append(", ");
                js.append("node").append(nodeId(node.getName()));
                first = false;
            }
            js.append("];\n");
            js.append("            \n");
            js.append("            // Create node elements\n");
            js.append("            nodes.forEach(node => createNodeElement(node));\n");
            js.append("            \n");
            js.append("            // Define edges\n");
            js.append("            edges = [\n");
            
            boolean firstEdge = true;
            for (Node fromNode : graph) {
                for (Node toNode : fromNode.getEdges()) {
                    if (!firstEdge) js.append(",\n");
                    js.append("                new GraphEdge(node").append(nodeId(fromNode.getName())).append(", node").append(nodeId(toNode.getName())).append(")");
                    firstEdge = false;
                }
            }
            
            js.append("\n            ];\n");
            js.append("            \n");
            js.append("            // Draw arrows\n");
            js.append("            edges.forEach(edge => drawArrow(edge.from, edge.to));\n");
        }
        
        js.append("        }\n");
        
        return js.toString();
    }
    
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
     * Generates the closing HTML tags.
     */
    private static String generateHTMLFooter() {
        return "</body>\n</html>";
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