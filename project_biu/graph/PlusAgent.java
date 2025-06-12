package graph;



/**
 * PlusAgent performs addition operation on two input values x and y.
 * It subscribes to the first two topics from the subs array and publishes 
 * the result (x + y) to the first topic in the pubs array.
 */
public class PlusAgent implements Agent {
    
    private String[] subs;  // Topics to subscribe to
    private String[] pubs;  // Topics to publish to
    private String name;    // Agent name
    
    // Internal values for the two operands
    private double x = 0.0;
    private double y = 0.0;
    
    // Flags to track if values have been received
    private boolean xReceived = false;
    private boolean yReceived = false;
    
    /**
     * Constructor for PlusAgent
     * @param subs Array of topic names to subscribe to (expects at least 2)
     * @param pubs Array of topic names to publish to (expects at least 1)
     */
    public PlusAgent(String[] subs, String[] pubs) {
        this.subs = subs;
        this.pubs = pubs;
        this.name = "PlusAgent";
        
        // Subscribe to the first two topics from subs array
        if (subs.length >= 2) {
            TopicManagerSingleton.get().getTopic(subs[0]).subscribe(this);
            TopicManagerSingleton.get().getTopic(subs[1]).subscribe(this);
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
        this.x = 0.0;
        this.y = 0.0;
        this.xReceived = false;
        this.yReceived = false;
    }

    @Override
    public void callback(String topic, Message msg) {
        // Check if the message contains a valid number
        if (!Double.isNaN(msg.asDouble)) {
            // Determine which operand to update based on the topic
            if (subs.length >= 2) {
                if (topic.equals(subs[0])) {
                    this.x = msg.asDouble;
                    this.xReceived = true;
                } else if (topic.equals(subs[1])) {
                    this.y = msg.asDouble;
                    this.yReceived = true;
                }
                
                // If both values are received and valid, calculate and publish result
                if (xReceived && yReceived) {
                    double result = x + y;
                    Message resultMsg = new Message(result);
                    
                    // Publish to the first topic in pubs array
                    if (pubs.length >= 1) {
                        TopicManagerSingleton.get().getTopic(pubs[0]).publish(resultMsg);
                    }
                }
            }
        }
    }

    @Override
    public void close() {
        // Clean up resources if needed
        // Unsubscribe from topics
        if (subs.length >= 2) {
            TopicManagerSingleton.get().getTopic(subs[0]).unsubscribe(this);
            TopicManagerSingleton.get().getTopic(subs[1]).unsubscribe(this);
        }
        
        // Remove as publisher
        if (pubs.length >= 1) {
            TopicManagerSingleton.get().getTopic(pubs[0]).removePublisher(this);
        }
    }
}
