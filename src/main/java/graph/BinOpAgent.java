package graph;

import java.util.function.BinaryOperator;
import java.util.*;

/**
 * Binary Operation Agent that performs mathematical operations on two input values
 * and publishes the result to an output topic
 */
public class BinOpAgent implements Agent {
    // Agent identifier/name
    private String op;
    // First input topic name
    private String input1;
    // Second input topic name
    private String input2;
    // Output topic name
    private String output;
    // Binary operation function to apply
    private BinaryOperator<Double> func;

    // Storage for input values
    private Double value1;
    private Double value2;

    // Static map of predefined operations
    public static final Map<String, BinaryOperator<Double>> operationMap = 
        Collections.unmodifiableMap(new HashMap<String, BinaryOperator<Double>>() {{
            put("plus", (x, y) -> x + y);
            put("minus", (x, y) -> x - y);
            put("mul", (x, y) -> x * y);
            put("div", (x, y) -> x / y);
            put("power", (x, y) -> Math.pow(x, y));
        }});

    /**
     * Constructor with explicit binary operator
     */
    public BinOpAgent(String op, String input1, String input2, String output, BinaryOperator<Double> func) {
        this.op = op;
        this.input1 = input1;
        this.input2 = input2;
        this.output = output;
        this.func = func;

        subscribeToInputs();
        registerAsPublisher();
    }

    /**
     * Constructor using predefined operation type
     */
    public BinOpAgent(String op, String[] inputs, String[] outputs, String operatorType) {
        this(op, inputs[0], inputs[1], outputs[0], operationMap.get(operatorType));
    }

    /**
     * Subscribe this agent to both input topics
     */
    private void subscribeToInputs() {
        Topic topic1 = TopicManagerSingleton.get().getTopic(input1);
        topic1.subscribe(this);
        
        Topic topic2 = TopicManagerSingleton.get().getTopic(input2);
        topic2.subscribe(this);
    }

    /**
     * Register this agent as publisher for the output topic
     */
    private void registerAsPublisher() {
        Topic outputTopic = TopicManagerSingleton.get().getTopic(output);
        outputTopic.addPublisher(this);
    }

    /**
     * Perform the binary operation and publish result
     */
    private void performCalculation() {
        Double result = func.apply(value1, value2);
        publishResult(result);
    }

    /**
     * Get the agent's name
     */
    public String getName() {
        return this.op;
    }

    /**
     * Reset the agent's input values to default state
     */
    public void reset() {
        this.value1 = 0.0;
        this.value2 = 0.0;
    }

    /**
     * Callback method invoked when messages arrive on subscribed topics
     * @param topic The topic name that received the message
     * @param msg The message containing the data
     */
    public void callback(String topic, Message msg) {
        // Check which input topic received the message
        if (topic.equals(input1)) {
            value1 = msg.asDouble;
        } else if (topic.equals(input2)) {
            value2 = msg.asDouble;
        }

        // Execute operation only when both inputs are available
        if (value1 != null && value2 != null)
            performCalculation();
    }

    /**
     * Clean up resources when agent is closed
     */
    public void close() {
        // Resource cleanup if needed
    }

    /**
     * Publish the calculated result to the output topic
     * @param result The computed result to publish
     */
    private void publishResult(Double result) {
        Topic outputTopic = TopicManagerSingleton.get().getTopic(output);
        outputTopic.publish(new Message(result));
    }
}