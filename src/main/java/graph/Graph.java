package graph;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * Graph class that extends ArrayList to represent a collection of nodes
 * Provides functionality for cycle detection, topic-based graph creation, and XML export
 */
public class Graph extends ArrayList<Node>{
    
    // Counter for generating unique vertex identifiers
    // private Integer vertexCount = 1;
    
    /**
     * Adds a node to the graph
     * @param node the node to add
     * @return true if the node was added successfully
     */
    public boolean addNode(Node node) {
        return this.add(node);
    }
    
    /**
     * Checks if the graph contains any cycles
     * @return true if cycles are detected, false otherwise
     */
    public boolean hasCycles() {
        // Iterate through each vertex in the graph
        for(Node vertex : this) {
            // Check if this vertex is part of a cycle
            if(vertex.hasCycles() == true)
                return true;
        }
        return false;
    }
    
    /**
     * Creates the graph structure based on topics from TopicManager
     * Builds nodes for topics and agents, establishing edges based on publisher/subscriber relationships
     */
    public void createFromTopics(){
        // Clear any existing graph data
        this.clear();
        
        // Map to track already processed agents and their corresponding nodes
        Map<Agent, Node> processedAgents = new HashMap<Agent, Node>();
        
        // Iterate through all topics from the TopicManager
        for (Topic currentTopic : TopicManagerSingleton.get().getTopics()) {
            
            // Create a node for the current topic
            Node topicVertex = new Node("T" + currentTopic.name);
            
            // Process all subscribers of this topic
            for(Agent subscriber : currentTopic.getSubscribers()) {
                
                // If this agent hasn't been processed yet, create a new node
                if(processedAgents.containsKey(subscriber) == false){
                    Node subscriberVertex = new Node("A" + subscriber.getName());
                    this.add(subscriberVertex);
                    processedAgents.put(subscriber, subscriberVertex);
                }
                // Create edge from topic to subscriber (topic sends data to subscriber)
                topicVertex.addEdge(processedAgents.get(subscriber));
            }
            
            // Process all publishers of this topic
            for(Agent publisher : currentTopic.getPublishers()) {
                
                // If this agent hasn't been processed yet, create a new node
                if(processedAgents.containsKey(publisher) == false){
                    Node publisherVertex = new Node("A" + publisher.getName());
                    this.add(publisherVertex);
                    processedAgents.put(publisher, publisherVertex);
                }
                // Create edge from publisher to topic (publisher sends data to topic)
                processedAgents.get(publisher).addEdge(topicVertex);
            }
            // Add the topic node to the graph
            this.add(topicVertex);
        }
    }
    
    /**
     * Generates an XML representation of the graph
     * @return XML string representing the graph structure
     * @throws Exception if XML generation fails
     */
    public String buildXMLRepresentation() throws Exception {
        // Create XML document builder factory and builder
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        
        // Create new XML document
        Document document = builder.newDocument();
        
        // Create root element for the graph
        Element root = document.createElement("graph");
        document.appendChild(root);
        
        // Iterate through all vertices in the graph
        for (Node vertex : this) {
            // Create XML element for each vertex
            Element vertexElement = document.createElement("node");
            vertexElement.setAttribute("id", vertex.getName());
            root.appendChild(vertexElement);
            
            // Add all edges for this vertex
            for (Node connection : vertex.getEdges()) {
                Element connectionElement = document.createElement("edge");
                connectionElement.setAttribute("to", connection.getName());
                vertexElement.appendChild(connectionElement);
            }
        }
        
        // Transform the document to a formatted XML string
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes"); // Enable pretty printing
        DOMSource source = new DOMSource(document);
        StringWriter output = new StringWriter();
        StreamResult result = new StreamResult(output);
        transformer.transform(source, result);
        
        return output.toString();
    }
}
