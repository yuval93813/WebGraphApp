import views.HtmlGraphWriter;
import graph.Graph;
import graph.Node;
import graph.TopicManagerSingleton;
import graph.Topic;
import graph.Message;
import java.util.List;

/**
 * Test class for HtmlGraphWriter to verify dynamic HTML generation
 */
public class TestHtmlGraphWriter {
    
    public static void main(String[] args) {
        System.out.println("Testing HtmlGraphWriter...");
        
        try {
            // Create a sample graph
            Graph graph = new Graph();
            
            // Add some sample nodes
            Node nodeA = new Node("A");
            Node nodeB = new Node("B");
            Node nodePlus = new Node("+");
            Node nodeResult = new Node("Result");
            
            nodeA.addEdge(nodePlus);
            nodeB.addEdge(nodePlus);
            nodePlus.addEdge(nodeResult);
            
            graph.addNode(nodeA);
            graph.addNode(nodeB);
            graph.addNode(nodePlus);
            graph.addNode(nodeResult);
            
            // Create some sample topics
            Topic topicA = TopicManagerSingleton.get().getTopic("inputA");
            Topic topicB = TopicManagerSingleton.get().getTopic("inputB");
            Topic topicResult = TopicManagerSingleton.get().getTopic("output");
            
            topicA.publish(new Message("10"));
            topicB.publish(new Message("20"));
            topicResult.publish(new Message("30"));
            
            // Generate HTML
            List<String> htmlLines = HtmlGraphWriter.getGraphHTML(graph);
            
            System.out.println("✅ HTML generation successful!");
            System.out.println("Generated " + htmlLines.size() + " lines of HTML");
            
            // Write to file for inspection
            try (java.io.PrintWriter writer = new java.io.PrintWriter("test_output.html")) {
                for (String line : htmlLines) {
                    writer.println(line);
                }
                System.out.println("✅ HTML written to test_output.html");
            }
            
            // Check for key components
            String fullHtml = String.join("\n", htmlLines);
            
            if (fullHtml.contains("graphData")) {
                System.out.println("✅ Graph data injection successful");
            } else {
                System.out.println("❌ Graph data not found in HTML");
            }
            
            if (fullHtml.contains("topicData")) {
                System.out.println("✅ Topic data injection successful");
            } else {
                System.out.println("❌ Topic data not found in HTML");
            }
            
            if (fullHtml.contains("Graph Statistics")) {
                System.out.println("✅ Graph statistics injection successful");
            } else {
                System.out.println("❌ Graph statistics not found in HTML");
            }
            
        } catch (Exception e) {
            System.out.println("❌ Error during testing: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("Testing completed.");
    }
}
