package project_biu.servlets;

import project_biu.configs.TopicManagerSingleton;
import project_biu.graph.Graph;
import project_biu.views.HtmlGraphWriter; // Corrected import
import project_biu.graph.Node; // Assuming Node is used by Graph or Topic, otherwise could be removed if not directly used.
import project_biu.graph.Topic;
import project_biu.server.RequestInfo;
import project_biu.server.Servlet;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

public class TopicDisplayer implements Servlet {

    @Override
    public void handle(RequestInfo ri, OutputStream toClient) throws IOException {
        try {
            TopicManagerSingleton tms = TopicManagerSingleton.get();
            Graph graph = new Graph();

            // Populate graph from TopicManagerSingleton
            // Assuming TopicManagerSingleton.getTopics() returns Collection<Topic>
            // And Topic has methods like getName(), getChildren(), getPublishers(), getSubscribers()
            // And Publisher/Subscriber (Agent) has a getName() method

            Collection<Topic> topics = tms.getTopics().values(); // Assuming getTopics returns a Map<String, Topic>

            for (Topic topic : topics) {
                graph.addNode(topic.getName(), "topic"); // Add topic node

                // Add edges for parent-child relationships (if applicable)
                // This part is a bit tricky without knowing the exact structure of Topic and how parent-child is defined.
                // Let's assume Topic has a getParent() or similar, or children are directly linked.
                // For now, we'll focus on publishers and subscribers as primary relationships.

                for (Topic subTopic : topic.getChildren()) {
                     graph.addNode(subTopic.getName(),"topic"); // Ensure child node exists
                     graph.addEdge(topic.getName(), subTopic.getName(), "sub-topic", "grey");
                }

                for (String publisherName : topic.getPublishers()) {
                    graph.addNode(publisherName, "agent"); // Add agent node
                    graph.addEdge(publisherName, topic.getName(), "publishes", "blue");
                }

                for (String subscriberName : topic.getSubscribers()) {
                    graph.addNode(subscriberName, "agent"); // Add agent node
                    graph.addEdge(topic.getName(), subscriberName, "subscribes", "green");
                }
            }

            // Remove orphan nodes if any (nodes that are agents but neither publish nor subscribe to any displayed topic)
            // This might be better handled in Graph.java or HtmlGraphWriter.java if it's a common requirement
            // For now, we assume all added agent nodes are relevant.

            HtmlGraphWriter graphWriter = new HtmlGraphWriter();
            // Assuming HtmlGraphWriter has a method to generate the full HTML page
            String htmlContent = graphWriter.getGraphHTML(graph);

            // Send headers
            String headers = "HTTP/1.1 200 OK\r\n" +
                             "Content-Type: text/html; charset=UTF-8\r\n" +
                             "Content-Length: " + htmlContent.getBytes(StandardCharsets.UTF_8).length + "\r\n" +
                             "\r\n";
            toClient.write(headers.getBytes(StandardCharsets.UTF_8));

            // Send HTML content
            toClient.write(htmlContent.getBytes(StandardCharsets.UTF_8));

        } catch (Exception e) {
            // Log the exception (e.g., using a proper logger)
            // System.err.println("Error generating topic graph: " + e.getMessage());
            // e.printStackTrace(); // For debugging

            // Send an error response
            String errorMessage = "HTTP/1.1 500 Internal Server Error\r\n" +
                                  "Content-Type: text/html; charset=UTF-8\r\n" +
                                  "\r\n" +
                                  "<html><body><h1>500 Internal Server Error</h1><p>Failed to generate topic graph: " +
                                  e.getClass().getSimpleName() + " - " + e.getMessage() +
                                  "</p></body></html>";
            try {
                toClient.write(errorMessage.getBytes(StandardCharsets.UTF_8));
            } catch (IOException ioe) {
                // If sending error fails, not much more we can do here
                // System.err.println("Critical error: Failed to send 500 response to client: " + ioe.getMessage());
            }
        }
    }

    @Override
    public void close() throws IOException {
        // No resources to close in this servlet
    }
}
