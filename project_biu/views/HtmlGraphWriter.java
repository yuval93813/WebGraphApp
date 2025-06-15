package project_biu.views;

import project_biu.graph.Graph;
import project_biu.graph.Node;
import project_biu.graph.Edge;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class HtmlGraphWriter {

    private static final int NODE_WIDTH = 100;
    private static final int NODE_HEIGHT = 60;
    private static final int HORIZONTAL_SPACING = 80;
    private static final int VERTICAL_SPACING = 80;
    private static final int SVG_PADDING = 50; // Padding around the graph area for SVG

    public String getGraphHTML(Graph graph) {
        if (graph == null) {
            return "<html><body><h1>Error: Graph is null</h1></body></html>";
        }

        StringBuilder html = new StringBuilder();
        Map<String, Node> nodeMap = new HashMap<>();
        for(Node n : graph.getNodes()){
            nodeMap.put(n.getName(), n);
        }


        // Assign positions to nodes (simple grid layout)
        Map<String, Point> nodePositions = assignNodePositions(graph.getNodes());

        // Calculate SVG dimensions
        int maxX = 0;
        int maxY = 0;
        for (Point p : nodePositions.values()) {
            if (p.x + NODE_WIDTH > maxX) maxX = p.x + NODE_WIDTH;
            if (p.y + NODE_HEIGHT > maxY) maxY = p.y + NODE_HEIGHT;
        }
        int svgWidth = maxX + SVG_PADDING * 2;
        int svgHeight = maxY + SVG_PADDING * 2;


        html.append("<!DOCTYPE html>\n");
        html.append("<html lang=\"en\">\n");
        html.append("<head>\n");
        html.append("    <meta charset=\"UTF-8\">\n");
        html.append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        html.append("    <title>Topic Graph</title>\n");
        html.append("    <style>\n");
        html.append("        body { font-family: sans-serif; margin: 0; background-color: #f4f4f4; display: flex; justify-content: center; align-items: center; min-height: 100vh; }\n");
        html.append("        .graph-container { position: relative; border: 1px solid #ccc; background-color: #fff; box-shadow: 0 0 10px rgba(0,0,0,0.1); overflow: auto; }\n");
        html.append("        .node {\n");
        html.append("            position: absolute;\n");
        html.append("            width: ").append(NODE_WIDTH).append("px;\n");
        html.append("            height: ").append(NODE_HEIGHT).append("px;\n");
        html.append("            border: 2px solid #333;\n");
        html.append("            border-radius: 8px;\n");
        html.append("            background-color: #lightblue;\n");
        html.append("            display: flex;\n");
        html.append("            flex-direction: column;\n");
        html.append("            justify-content: center;\n");
        html.append("            align-items: center;\n");
        html.append("            text-align: center;\n");
        html.append("            font-size: 12px;\n");
        html.append("            box-sizing: border-box;\n");
        html.append("            padding: 5px;\n");
        html.append("        }\n");
        html.append("        .node .name { font-weight: bold; margin-bottom: 4px; }\n");
        html.append("        .node .type { font-style: italic; font-size: 10px; color: #555; }\n");
        html.append("        .node.topic { background-color: #lightgreen; border-color: #28a745; }\n");
        html.append("        .node.agent { background-color: #ffcccb; border-color: #dc3545; }\n");
        html.append("        .edge-label { font-size: 10px; fill: #333; text-anchor: middle; }\n");
        html.append("        marker { fill: #333; }\n");
        html.append("    </style>\n");
        html.append("</head>\n");
        html.append("<body>\n");
        html.append("    <div class=\"graph-container\" style=\"width: ").append(svgWidth).append("px; height: ").append(svgHeight).append("px;\">\n");
        html.append("        <svg width=\"").append(svgWidth).append("\" height=\"").append(svgHeight).append("\" style=\"position: absolute; top: 0; left: 0; z-index: 0;\">\n");
        html.append("            <defs>\n");
        html.append("                <marker id=\"arrowhead\" markerWidth=\"10\" markerHeight=\"7\" refX=\"0\" refY=\"3.5\" orient=\"auto\">\n");
        html.append("                    <polygon points=\"0 0, 10 3.5, 0 7\" />\n");
        html.append("                </marker>\n");
        html.append("            </defs>\n");

        // Draw edges
        for (Edge edge : graph.getEdges()) {
            Node fromNode = nodeMap.get(edge.getFrom());
            Node toNode = nodeMap.get(edge.getTo());
            if (fromNode == null || toNode == null) continue;

            Point fromPos = nodePositions.get(fromNode.getName());
            Point toPos = nodePositions.get(toNode.getName());
            if (fromPos == null || toPos == null) continue;

            // Calculate edge start and end points (center of nodes)
            double startX = fromPos.x + NODE_WIDTH / 2.0;
            double startY = fromPos.y + NODE_HEIGHT / 2.0;
            double endX = toPos.x + NODE_WIDTH / 2.0;
            double endY = toPos.y + NODE_HEIGHT / 2.0;

            // Adjust start/end points to be on the border of the node, not center
            // This is a simple adjustment, more precise calculations would involve exact intersection with node shape
            double angle = Math.atan2(endY - startY, endX - startX);
            startX += (NODE_WIDTH / 2.0) * Math.cos(angle);
            startY += (NODE_HEIGHT / 2.0) * Math.sin(angle);
            endX -= (NODE_WIDTH / 2.0) * Math.cos(angle);
            endY -= (NODE_HEIGHT / 2.0) * Math.sin(angle);


            String color = edge.getColor() != null ? edge.getColor() : "#333"; // Default edge color
            html.append("            <line x1=\"").append(startX).append("\" y1=\"").append(startY)
                .append("\" x2=\"").append(endX).append("\" y2=\"").append(endY)
                .append("\" stroke=\"").append(color).append("\" stroke-width=\"2\" marker-end=\"url(#arrowhead)\"/>\n");

            // Edge label position (midpoint)
            double midX = (startX + endX) / 2;
            double midY = (startY + endY) / 2;
            html.append("            <text x=\"").append(midX).append("\" y=\"").append(midY - 5) // Offset label slightly above line
                .append("\" class=\"edge-label\">").append(escapeHtml(edge.getType())).append("</text>\n");
        }
        html.append("        </svg>\n");

        // Draw nodes
        for (Node node : graph.getNodes()) {
            Point pos = nodePositions.get(node.getName());
            if (pos == null) continue; // Should not happen if assignNodePositions is correct

            String nodeClass = "node " + (node.getType() != null ? node.getType().toLowerCase() : "");
            html.append("        <div class=\"").append(nodeClass).append("\" style=\"left: ").append(pos.x).append("px; top: ").append(pos.y).append("px; z-index: 1;\">\n");
            html.append("            <div class=\"name\">").append(escapeHtml(node.getName())).append("</div>\n");
            html.append("            <div class=\"type\">(").append(escapeHtml(node.getType())).append(")</div>\n");
            html.append("        </div>\n");
        }

        html.append("    </div>\n");
        html.append("</body>\n");
        html.append("</html>\n");

        return html.toString();
    }

    // Simple layout algorithm (distribute in a grid, then center)
    private Map<String, Point> assignNodePositions(Collection<Node> nodes) {
        Map<String, Point> positions = new HashMap<>();
        if (nodes == null || nodes.isEmpty()) {
            return positions;
        }

        List<Node> nodeList = new ArrayList<>(nodes);
        int numNodes = nodeList.size();
        int cols = (int) Math.ceil(Math.sqrt(numNodes)); // Try to make it squarish
        int rows = (int) Math.ceil((double) numNodes / cols);

        int currentX = SVG_PADDING;
        int currentY = SVG_PADDING;
        int maxNodesInRow = cols;

        for (int i = 0; i < numNodes; i++) {
            Node node = nodeList.get(i);
            positions.put(node.getName(), new Point(currentX, currentY));
            currentX += NODE_WIDTH + HORIZONTAL_SPACING;
            if ((i + 1) % maxNodesInRow == 0) {
                currentX = SVG_PADDING;
                currentY += NODE_HEIGHT + VERTICAL_SPACING;
            }
        }
        return positions;
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }

    // Helper class for coordinates
    private static class Point {
        int x, y;
        Point(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
}
