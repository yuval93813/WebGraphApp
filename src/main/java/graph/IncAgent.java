package graph;

/**
 * IncAgent increments a numeric value by 1.
 * It subscribes to the first topic from the subs array and publishes 
 * the incremented result to the first topic in the pubs array.
 */
public class IncAgent implements Agent {
    
    private String[] subs;  // Topics to subscribe to
    private String[] pubs;  // Topics to publish to
    private String name;    // Agent name
    
    /**
     * Constructor for IncAgent
     * @param subs Array of topic names to subscribe to (expects at least 1)
     * @param pubs Array of topic names to publish to (expects at least 1)
     */
    public IncAgent(String[] subs, String[] pubs) {
        this.subs = subs;
        this.pubs = pubs;
        this.name = "IncAgent";
        
        // Subscribe to the first topic from subs array
        if (subs.length >= 1) {
            TopicManagerSingleton.get().getTopic(subs[0]).subscribe(this);
        }
        
        // Register as publisher for the first topic in pubs array
        if (pubs.length >= 1) {
            TopicManagerSingleton.get().getTopic(pubs[0]).addPublisher(this);
        }
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void reset() {
        // No internal state to reset for IncAgent
    }

    @Override
    public void callback(String topic, Message msg) {
        // Check if the message contains a valid number
        if (!Double.isNaN(msg.asDouble)) {
            // Increment the value by 1
            double result = msg.asDouble + 1.0;
            Message resultMsg = new Message(result);
            
            // Publish to the first topic in pubs array
            if (pubs.length >= 1) {
                TopicManagerSingleton.get().getTopic(pubs[0]).publish(resultMsg);
            }
        }
    }

    @Override
    public void close() {
        // Clean up resources if needed
        // Unsubscribe from topics
        if (subs.length >= 1) {
            TopicManagerSingleton.get().getTopic(subs[0]).unsubscribe(this);
        }
        
        // Remove as publisher
        if (pubs.length >= 1) {
            TopicManagerSingleton.get().getTopic(pubs[0]).removePublisher(this);
        }
    }
}
