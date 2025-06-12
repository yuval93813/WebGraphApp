package graph;

import java.util.*;

/**
 * Represents a node in a directed graph structure.
 * Each node has a name, a list of connected nodes (edges), and can store a message.
 */
public class Node {
    // The unique identifier/name for this node
    private String name;
    // List of nodes that this node has directed edges to
    private List<Node> edges;
    // Message object that this node can hold
    private Message msg;

    /**
     * Constructor to create a new node with the given name.
     * Initializes the edges list as an empty ArrayList.
     * @param nodeName The name to assign to this node
     */
    public Node(String nodeName){
        this.name = nodeName;
        this.edges = new ArrayList<Node>();
    }

    // Getters & Setters
    
    /**
     * Gets the name of this node.
     * @return The node's name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of this node.
     * @param nodeName The new name for this node
     */
    public void setName(String nodeName) {
        this.name = nodeName;
    }

    /**
     * Gets the list of nodes that this node has edges to.
     * @return List of connected nodes
     */
    public List<Node> getEdges() {
        return edges;
    }

    /**
     * Sets the entire edges list for this node.
     * @param connections New list of connected nodes
     */
    public void setEdges(List<Node> connections) {
        this.edges = connections;
    }

    /**
     * Gets the message stored in this node.
     * @return The message object, or null if none is set
     */
    public Message getMsg() {
        return msg;
    }

    /**
     * Sets a message for this node.
     * @param message The message object to store
     */
    public void setMsg(Message message) {
        this.msg = message;
    }

    /**
     * Adds a directed edge from this node to the target node.
     * @param targetNode The node to connect to
     */
    public void addEdge(Node targetNode){
        this.edges.add(targetNode);
    }

    /**
     * Determines if this node is part of any cycle in the graph.
     * Uses depth-first search with recursion stack tracking.
     * @return true if a cycle is detected, false otherwise
     */
    public boolean hasCycles() {
        // Track all nodes visited during the entire search
        Set<Node> visitedNodes = new HashSet<>();
        // Track nodes in the current recursive path
        Set<Node> currentPath = new HashSet<>();

        // Use depth-first search to detect cycles
        return detectCycle(this, visitedNodes, currentPath);
    }

    /**
     * Helper method that performs depth-first search to detect cycles.
     * A cycle exists if we encounter a node that's already in our current path.
     * @param node The current node being processed
     * @param visitedNodes Set of all nodes visited so far
     * @param currentPath Set of nodes in the current recursive path
     * @return true if a cycle is found, false otherwise
     */
    private boolean detectCycle(Node node, Set<Node> visitedNodes, Set<Node> currentPath) {
        // Mark current node as visited and add to current path
        visitedNodes.add(node);
        currentPath.add(node);

        // Check all connected nodes
        for (Node connectedNode : node.getEdges()) {
            // If connected node hasn't been visited, continue DFS
            if (!visitedNodes.contains(connectedNode)) {
                if (detectCycle(connectedNode, visitedNodes, currentPath)) {
                    return true; // Cycle found in recursive call
                }
            }
            // If connected node is in current path, cycle found
            else if (currentPath.contains(connectedNode)) {
                return true; // Back edge detected - cycle exists
            }
        }

        // Remove current node from path before backtracking
        // This is crucial for proper cycle detection
        currentPath.remove(node);
        return false; // No cycle found from this path
    }
}