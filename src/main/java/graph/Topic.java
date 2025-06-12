package graph;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a communication topic in a publish-subscribe messaging system.
 * 
 * A Topic acts as a communication channel where:
 * - Publishers can send messages to the topic
 * - Subscribers receive all messages published to the topic
 * - The topic maintains lists of both publishers and subscribers
 * - The topic stores the most recent message content as a result
 * 
 * This implementation follows the Observer pattern, where the topic notifies
 * all subscribers when a new message is published.
 */
public class Topic {

    /**
     * The unique name identifier for this topic.
     * This name is used to identify and reference the topic throughout the system.
     */
    public final String name;
    
    /**
     * List of agents that are subscribed to receive messages from this topic.
     * When a message is published, all subscribers in this list will be notified.
     */
    private List<Agent> subscribers;
    
    /**
     * List of agents that are authorized to publish messages to this topic.
     * This list is maintained for tracking and potential access control purposes.
     */
    private List<Agent> publishers;

    /**
     * Stores the text content of the most recently published message.
     * This allows quick access to the lagraph state/value of the topic.
     */
    private String result = "";    /**
     * Creates a new Topic with the specified name.
     * Initializes empty lists for subscribers and publishers.
     * 
     * @param name The unique identifier for this topic
     */
    Topic(String name){
        this.name = name;
        this.subscribers = new ArrayList<Agent>();
        this.publishers = new ArrayList<Agent>();
    }

    // ==================== Subscription Management ====================
    
    /**
     * Adds an agent to the list of subscribers for this topic.
     * The agent will receive notifications for all future messages published to this topic.
     * 
     * @param agent The agent to subscribe to this topic
     */
    public void subscribe(Agent agent){
        this.subscribers.add(agent);
    }
    
    /**
     * Removes an agent from the list of subscribers for this topic.
     * The agent will no longer receive notifications for messages published to this topic.
     * 
     * @param agent The agent to unsubscribe from this topic
     */
    public void unsubscribe(Agent agent){
        this.subscribers.remove(agent);
    }

    // ==================== Message Publishing ====================
    
    /**
     * Publishes a message to this topic.
     * This method performs two main actions:
     * 1. Updates the topic's result with the message content
     * 2. Notifies all subscribed agents about the new message
     * 
     * Each subscriber's callback method is invoked with the topic name and message.
     * 
     * @param message The message to publish to all subscribers
     */
    public void publish(Message message){
        // Store the lagraph message content for quick access
        setResult(message.asText);

        // Notify all subscribers about the new message
        for(Agent agent : this.subscribers){
            agent.callback(this.name, message);
        }
    }

    // ==================== Publisher Management ====================
    
    /**
     * Adds an agent to the list of authorized publishers for this topic.
     * This is primarily for tracking purposes and potential access control.
     * 
     * @param agent The agent to add as a publisher
     */
    public void addPublisher(Agent agent){
        this.publishers.add(agent);
    }

    /**
     * Removes an agent from the list of authorized publishers for this topic.
     * 
     * @param agent The agent to remove as a publisher
     */
    public void removePublisher(Agent agent){
        this.publishers.remove(agent);
    }

    // ==================== Getter Methods ====================
    
    /**
     * Returns a list of all agents that are authorized to publish to this topic.
     * 
     * @return List of publisher agents
     */
    public List<Agent> getPublishers(){
        return this.publishers;
    }
    
    /**
     * Returns a list of all agents that are subscribed to receive messages from this topic.
     * 
     * @return List of subscriber agents
     */
    public List<Agent> getSubscribers(){
        return this.subscribers;
    }

    /**
     * Returns the text content of the most recently published message.
     * This provides quick access to the current state/value of the topic.
     * 
     * @return The lagraph message content as a string
     */
    public String getResult() {
        return this.result;
    }

    /**
     * Sets the result field to store the lagraph message content.
     * This method is called internally when a message is published.
     * 
     * @param anyResult The string content to store as the lagraph result
     */
    public void setResult(String anyResult){
        this.result = anyResult;
    }
}