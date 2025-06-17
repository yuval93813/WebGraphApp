package views;

import graph.Graph;
import graph.Node;
import graph.Message;
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
        
        // Center the graph in a 800x600 viewBox
        int cx = 400, cy = 300, r = Math.min(180, 250 - n * 5); // Adjust radius based on node count
        Map<Node, Integer[]> nodePos = new HashMap<>();
        Map<Node, Integer> nodeRadii = new HashMap<>(); // Store calculated radii for each node
        int i = 0;
        
        // First pass: find maximum agent radius
        int maxAgentRadius = 25; // Default minimum
        for (Node node : g) {
            String nodeName = node.getName();
            String displayName = nodeName;
            if (displayName.startsWith("T") || displayName.startsWith("A")) {
                displayName = displayName.substring(1);
            }
            
            // Only check agents (not topics) for maximum radius
            if (nodeName.startsWith("A")) {
                int textWidth = displayName.length() * 8;
                int requiredRadius = Math.max(25, textWidth / 2 + 10);
                maxAgentRadius = Math.max(maxAgentRadius, requiredRadius);
            }
        }
        
        // Second pass: calculate positions and assign sizes
        for (Node node : g) {
            double angle = 2 * Math.PI * i / Math.max(1, n);
            int x = (int)(cx + r * Math.cos(angle));
            int y = (int)(cy + r * Math.sin(angle));
            nodePos.put(node, new Integer[]{x, y});
            
            String nodeName = node.getName();
            String displayName = nodeName;
            if (displayName.startsWith("T") || displayName.startsWith("A")) {
                displayName = displayName.substring(1);
            }
            
            if (nodeName.startsWith("A")) {
                // All agents use the same maximum radius
                nodeRadii.put(node, maxAgentRadius);
            } else {
                // Topics remain adaptive to their individual text length
                int textWidth = displayName.length() * 8;
                int minRadius = Math.max(25, textWidth / 2 + 10);
                nodeRadii.put(node, minRadius);
            }
            
            i++;
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
                // Topics as adaptive rectangles
                int rectWidth = Math.max(nodeRadius * 2, displayName.length() * 10 + 20);
                int rectHeight = 40;
                svg.add(String.format("<rect class='topic-node' x='%d' y='%d' width='%d' height='%d' rx='8' />", 
                    pos[0] - rectWidth/2, pos[1] - rectHeight/2, rectWidth, rectHeight));
                svg.add(String.format("<text x='%d' y='%d' text-anchor='middle' alignment-baseline='middle' class='topic-text'>%s</text>", 
                    pos[0], pos[1], displayName));
                // Show topic value above the topic
                svg.add(String.format("<text x='%d' y='%d' text-anchor='middle' alignment-baseline='baseline' class='value-text'>%s</text>", 
                    pos[0], pos[1] - rectHeight/2 - 15, msgText));
            } else {
                // Agents as adaptive circles
                svg.add(String.format("<circle class='agent-node' cx='%d' cy='%d' r='%d'/>", pos[0], pos[1], nodeRadius));
                svg.add(String.format("<text x='%d' y='%d' text-anchor='middle' alignment-baseline='middle' class='agent-text'>%s</text>", 
                    pos[0], pos[1], displayName));
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
