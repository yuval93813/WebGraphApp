package views;

import graph.Graph;
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
        svg.add("<marker id=\"arrowhead\" markerWidth=\"10\" markerHeight=\"10\" refX=\"8\" refY=\"3\" orient=\"auto\" markerUnits=\"strokeWidth\">");
        svg.add("<polygon points=\"0,0 0,6 9,3\" fill=\"#333\" stroke=\"#333\" stroke-width=\"1\"/>");
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
        int i = 0;
        
        for (Node node : g) {
            double angle = 2 * Math.PI * i / Math.max(1, n);
            int x = (int)(cx + r * Math.cos(angle));
            int y = (int)(cy + r * Math.sin(angle));
            nodePos.put(node, new Integer[]{x, y});
            i++;
        }
        
        // Edges with arrowheads
        for (Node node : g) {
            for (Node out : node.getEdges()) {
                Integer[] from = nodePos.get(node);
                Integer[] to = nodePos.get(out);
                if (from != null && to != null) {
                    svg.add(String.format("<line x1='%d' y1='%d' x2='%d' y2='%d' stroke='#333' stroke-width='2' marker-end='url(#arrowhead)' />", from[0], from[1], to[0], to[1]));
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
            
            // Get message/value for the node
            Object msg = null;
            try { msg = node.getMsg(); } catch (Exception e) {}
            String msgText = (msg != null) ? msg.toString() : "unknown";
            
            if (isTopic) {
                // Topics as rectangles
                svg.add(String.format("<rect class='topic-node' x='%d' y='%d' width='56' height='40' rx='8' />", 
                    pos[0] - 28, pos[1] - 20));
                svg.add(String.format("<text x='%d' y='%d' text-anchor='middle' alignment-baseline='middle' class='topic-text'>%s</text>", 
                    pos[0], pos[1], displayName));
                // Show topic value above the topic
                svg.add(String.format("<text x='%d' y='%d' text-anchor='middle' alignment-baseline='baseline' class='value-text'>%s</text>", 
                    pos[0], pos[1] - 35, msgText));
            } else {
                // Agents as circles
                svg.add(String.format("<circle class='agent-node' cx='%d' cy='%d' r='28'/>", pos[0], pos[1]));
                svg.add(String.format("<text x='%d' y='%d' text-anchor='middle' alignment-baseline='middle' class='agent-text'>%s</text>", 
                    pos[0], pos[1], displayName));
                // Show agent result above the agent
                svg.add(String.format("<text x='%d' y='%d' text-anchor='middle' alignment-baseline='baseline' class='result-text'>%s</text>", 
                    pos[0], pos[1] - 35, msgText));
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
