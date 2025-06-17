package views;

import graph.Graph;
import graph.Message;
import graph.Node;
import java.util.*;

public class HtmlGraphWriter {
    /**
     * Returns SVG lines for the current graph visualization (nodes, edges, labels).
     * This can be injected into the <svg> element in graph.html.
     */
    public static List<String> getGraphSVG(Graph g) {
        List<String> svg = new ArrayList<>();
        // Add SVG viewBox and arrowhead marker definition
        svg.add("<defs>");
        svg.add("<marker id=\"arrowhead\" markerWidth=\"12\" markerHeight=\"12\" refX=\"10\" refY=\"3\" orient=\"auto\" markerUnits=\"strokeWidth\">");
        svg.add("<polygon points=\"0,0 0,6 10,3\" fill=\"#333\" stroke=\"#333\" stroke-width=\"1\"/>");
        svg.add("</marker>");
        svg.add("</defs>");
        
        int n = g.size();
        if (n == 0) {
            svg.add("<text x='50%' y='50%' text-anchor='middle' alignment-baseline='middle' fill='#999' font-size='16'>No nodes in graph</text>");
            return svg;
        }
        
        // Center the graph in a LARGER viewBox and spread elements across it
        int canvasWidth = 800;   // Full width of viewBox
        int canvasHeight = 600;   // Full height of viewBox
        Map<Node, Integer[]> nodePos = new HashMap<>();
        Map<Node, Integer> nodeRadii = new HashMap<>();

        // First pass: FIXED agent radius
        int maxAgentRadius = 40; // FIXED SIZE - changed from 35 to 40

        // Second pass: SPREAD ELEMENTS ACROSS FULL CANVAS using grid layout
        List<Node> nodeList = new ArrayList<>();
        for (Node node : g) {
            nodeList.add(node);
        }

        // Calculate grid dimensions to spread nodes across full canvas
        int cols = (int) Math.ceil(Math.sqrt(n));
        int rows = (int) Math.ceil((double) n / cols);

        // Calculate spacing to use full canvas
        int horizontalSpacing = (canvasWidth - 200) / Math.max(1, cols - 1); // 200px margin (100px each side)
        int verticalSpacing = (canvasHeight - 200) / Math.max(1, rows - 1);   // 200px margin (100px each side)

        // Starting positions (with margins)
        int startX = 100; // Left margin
        int startY = 100; // Top margin

        // If only one row/col, center the elements
        if (cols == 1) {
            startX = canvasWidth / 2;
        }
        if (rows == 1) {
            startY = canvasHeight / 2;
        }

        for (int i = 0; i < n; i++) {
            Node node = nodeList.get(i);
            
            // Calculate grid position
            int row = i / cols;
            int col = i % cols;
            
            // Calculate actual pixel position
            int x, y;
            
            if (cols == 1) {
                x = startX; // Center horizontally if single column
            } else {
                x = startX + col * horizontalSpacing;
            }
            
            if (rows == 1) {
                y = startY; // Center vertically if single row
            } else {
                y = startY + row * verticalSpacing;
            }
            
            nodePos.put(node, new Integer[]{x, y});
            
            // Assign node radius (keep existing logic)
            String nodeName = node.getName();
            String displayName = nodeName;
            if (displayName.startsWith("T") || displayName.startsWith("A")) {
                displayName = displayName.substring(1);
            }
            
            if (nodeName.startsWith("A")) {
                nodeRadii.put(node, 40); // FIXED SIZE - changed from 35 to 40
            } else {
                int textWidth = displayName.length() * 8;
                int minRadius = Math.max(25, textWidth / 2 + 10);
                nodeRadii.put(node, minRadius);
            }
        }
        
        // Edges with arrowheads - calculate connection points to avoid overlapping with nodes
        for (Node node : g) {
            for (Node out : node.getEdges()) {
                Integer[] from = nodePos.get(node);
                Integer[] to = nodePos.get(out);
                if (from != null && to != null) {
                    // Calculate direction vector
                    double dx = to[0] - from[0];
                    double dy = to[1] - from[1];
                    double length = Math.sqrt(dx * dx + dy * dy);
                    
                    if (length > 0) {
                        // Normalize direction vector
                        dx /= length;
                        dy /= length;
                        
                        // Add extra space for arrow marker (arrow extends ~12px from line end)
                        int arrowSpace = 12;
                        
                        // Calculate connection points based on node types (circle vs rectangle)
                        int fromX, fromY, toX, toY;
                        
                        // From node connection point
                        if (node.getName().startsWith("T")) {
                            // Topic (rectangle) - calculate intersection with rectangle edge
                            int fromNodeRadius = nodeRadii.get(node);
                            int rectWidth = Math.max(fromNodeRadius * 2, node.getName().substring(1).length() * 10 + 20);
                            int rectHeight = 40;
                            
                            // Find intersection with rectangle edge
                            double intersectDist = Math.min(Math.abs((rectWidth/2.0) / dx), Math.abs((rectHeight/2.0) / dy));
                            fromX = (int)(from[0] + dx * intersectDist);
                            fromY = (int)(from[1] + dy * intersectDist);
                        } else {
                            // Agent (circle)
                            int fromRadius = nodeRadii.get(node);
                            fromX = (int)(from[0] + dx * fromRadius);
                            fromY = (int)(from[1] + dy * fromRadius);
                        }
                        
                        // To node connection point
                        if (out.getName().startsWith("T")) {
                            // Topic (rectangle) - calculate intersection with rectangle edge plus arrow space
                            int toNodeRadius = nodeRadii.get(out);
                            int rectWidth = Math.max(toNodeRadius * 2, out.getName().substring(1).length() * 10 + 20);
                            int rectHeight = 40;
                            
                            // Find intersection with rectangle edge and add arrow space
                            double intersectDist = Math.min(Math.abs((rectWidth/2.0) / Math.abs(dx)), Math.abs((rectHeight/2.0) / Math.abs(dy)));
                            toX = (int)(to[0] - dx * (intersectDist + arrowSpace));
                            toY = (int)(to[1] - dy * (intersectDist + arrowSpace));
                        } else {
                            // Agent (circle)
                            int toRadius = nodeRadii.get(out);
                            toX = (int)(to[0] - dx * (toRadius + arrowSpace));
                            toY = (int)(to[1] - dy * (toRadius + arrowSpace));
                        }
                        
                        svg.add(String.format("<line x1='%d' y1='%d' x2='%d' y2='%d' stroke='#333' stroke-width='2' marker-end='url(#arrowhead)' />", 
                            fromX, fromY, toX, toY));
                    }
                }
            }
        }
        
        // Nodes and labels
        for (Node node : g) {
            Integer[] pos = nodePos.get(node);
            String nodeName = node.getName();
            
            // Remove "T" or "A" prefix from node name for display
            String displayName = nodeName;
            if (displayName.startsWith("T") || displayName.startsWith("A")) {
                displayName = displayName.substring(1);
            }
            
            // Determine if this is a topic (starts with T) or agent (starts with A)
            boolean isTopic = nodeName.startsWith("T");
            
            // Get adaptive size for this node
            int nodeRadius = nodeRadii.get(node);
            
            // Get message/value for the node
            Object msg = null;
            try { msg = node.getMsg(); } catch (Exception e) {}
            String msgText = "unknown";
            if (msg != null) {
                if (msg instanceof Message) {
                    // Extract the actual text from the Message object
                    msgText = ((Message) msg).asText;
                } else {
                    msgText = msg.toString();
                }
            }
            
            if (isTopic) {
                // Topics as adaptive rectangles with message INSIDE - MAINTAIN MINIMUM SIZE
                int nameWidth = displayName.length() * 10 + 20;
                int msgWidth = msgText.length() * 8 + 20;
                int minWidth = Math.max(nodeRadius * 2, 80); // MINIMUM width of 80px
                int rectWidth = Math.max(minWidth, Math.max(nameWidth, msgWidth));
                int rectHeight = 60; // Fixed height to fit both name and message
                
                svg.add(String.format("<rect class='topic-node' x='%d' y='%d' width='%d' height='%d' rx='8' />", 
                    pos[0] - rectWidth/2, pos[1] - rectHeight/2, rectWidth, rectHeight));
                // Topic name in the upper part of rectangle
                svg.add(String.format("<text x='%d' y='%d' text-anchor='middle' alignment-baseline='middle' class='topic-text'>%s</text>", 
                    pos[0], pos[1] - 10, displayName));
                // Message in the lower part of rectangle (INSIDE THE BOX)
                svg.add(String.format("<text x='%d' y='%d' text-anchor='middle' alignment-baseline='middle' class='value-text' fill='white' font-size='11px'>%s</text>", 
                    pos[0], pos[1] + 12, msgText));
            } else {
                // Agents as FIXED SIZE circles with ONLY NAME (no message)
                int fixedRadius = 40; // FIXED SIZE - changed from 35 to 40
                
                svg.add(String.format("<circle class='agent-node' cx='%d' cy='%d' r='%d'/>", pos[0], pos[1], fixedRadius));
                // Agent name CENTERED in circle (no message)
                svg.add(String.format("<text x='%d' y='%d' text-anchor='middle' alignment-baseline='middle' class='agent-text' font-size='12px'>%s</text>", 
                    pos[0], pos[1], displayName)); // Agent name centered
            }
        }
        return svg;
    }

    /**
     * Returns HTML for the info panel for a given node (topic/message).
     * This can be injected into the #graphInfo div in graph.html.
     */
    public static String getTopicInfo(Node node) {
        if (node == null) return "<p>No node selected.</p>";
        StringBuilder sb = new StringBuilder();
        
        // Remove "T" or "A" prefix from node name for display
        String displayName = node.getName();
        if (displayName.startsWith("T") || displayName.startsWith("A")) {
            displayName = displayName.substring(1);
        }
        
        sb.append("<p><strong>Selected Node:</strong> ").append(displayName).append("</p>");
        Object msg = null;
        try { msg = node.getMsg(); } catch (Exception e) {}
        if (msg != null) {
            sb.append("<p><strong>Message:</strong> ").append(msg.toString()).append("</p>");
        }
        return sb.toString();
    }
}
