package project_biu.tests.views;

import project_biu.graph.Graph;
import project_biu.graph.Node; // Assuming Node is in project_biu.graph
import project_biu.graph.Edge; // Assuming Edge is in project_biu.graph
import project_biu.views.HtmlGraphWriter;

public class HtmlGraphWriterTest {

    public static void main(String[] args) {
        System.out.println("Running HtmlGraphWriterTest...");
        testSimpleGraph();
        // Add more test methods if needed and call them here
        System.out.println("HtmlGraphWriterTest completed.");
    }

    private static void testSimpleGraph() {
        System.out.print("  testSimpleGraph: ");
        Graph graph = new Graph();
        graph.addNode("NodeA", "topic");
        graph.addNode("NodeB", "agent");
        graph.addNode("NodeC", "topic");

        graph.addEdge("NodeA", "NodeB", "publishes", "blue");
        graph.addEdge("NodeB", "NodeC", "subscribes", "green");
        graph.addEdge("NodeA", "NodeC", "related_to", "grey");


        HtmlGraphWriter writer = new HtmlGraphWriter();
        String htmlOutput = writer.getGraphHTML(graph);

        // Basic assertions
        boolean pass = true;
        if (htmlOutput == null || htmlOutput.isEmpty()) {
            System.err.println("\n    FAIL: HTML output is null or empty.");
            pass = false;
        }
        if (!htmlOutput.startsWith("<!DOCTYPE html>")) {
            System.err.println("\n    FAIL: HTML output does not start with <!DOCTYPE html>.");
            pass = false;
        }
        if (!htmlOutput.contains("<html>")) {
            System.err.println("\n    FAIL: HTML output does not contain <html> tag.");
            pass = false;
        }
        if (!htmlOutput.endsWith("</html>\n")) { // As per current HtmlGraphWriter output
            System.err.println("\n    FAIL: HTML output does not end with </html>\\n.");
            pass = false;
        }
        if (!htmlOutput.contains("<div class=\"node topic\"") || !htmlOutput.contains("<div class=\"node agent\"")) {
            System.err.println("\n    FAIL: HTML output does not contain expected node divs with classes.");
            pass = false;
        }
        if (!htmlOutput.contains("NodeA") || !htmlOutput.contains("NodeB") || !htmlOutput.contains("NodeC")) {
            System.err.println("\n    FAIL: HTML output does not contain node names.");
            pass = false;
        }
        if (!htmlOutput.contains("<line x1=") || !htmlOutput.contains("stroke=\"blue\"") || !htmlOutput.contains("marker-end=\"url(#arrowhead)\"")) {
            System.err.println("\n    FAIL: HTML output does not contain expected SVG line for edges.");
            pass = false;
        }
        if (!htmlOutput.contains("publishes") || !htmlOutput.contains("subscribes") || !htmlOutput.contains("related_to")) {
            System.err.println("\n    FAIL: HTML output does not contain edge types.");
            pass = false;
        }


        if (pass) {
            System.out.println("PASS");
        } else {
            System.err.println("    Please review errors above.");
            // Optionally print a snippet of the output for debugging
            // System.err.println("    Output snippet: " + htmlOutput.substring(0, Math.min(htmlOutput.length(), 200)));
        }
    }
}
