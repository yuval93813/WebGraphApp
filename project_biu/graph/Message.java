package graph;

import java.util.Date;

/**
 * Represents a message that can be published to topics and received by subscribers.
 * This class provides multiple representations of the same data (text, numeric, and binary)
 * to allow flexible message handling across different types of agents.
 * 
 * The Message class is immutable - all fields are final and cannot be modified after creation.
 */
public class Message {

    /**
     * The raw binary representation of the message data.
     * This field stores the message content as a byte array for efficient storage and transmission.
     */
    public final byte[] data;
    
    /**
     * The string representation of the message.
     * This is the primary textual content of the message.
     */
    public final String asText;
    
    /**
     * The numeric representation of the message.
     * If the message cannot be parsed as a number, this field will contain Double.NaN.
     * This allows agents to easily work with numeric data without additional parsing.
     */
    public final double asDouble;
    
    /**
     * The timestamp when this message was created.
     * Automatically set to the current date and time during message construction.
     */
    public final Date date;    /**
     * Creates a new Message from a string input.
     * This is the primary constructor that initializes all message representations.
     * 
     * @param newMessage The string content of the message
     */
    public Message(String newMessage) {
        this.asText = newMessage;

        // Attempt to parse the string as a double for numeric operations
        // If parsing fails, asDouble will be set to NaN to indicate non-numeric content
        double tempDouble;
        try {
            tempDouble = Double.parseDouble(newMessage);
        } catch (NumberFormatException e) {
            tempDouble = Double.NaN;
        }

        this.asDouble = tempDouble;
        this.date = new Date(); // Capture the current timestamp
        this.data = newMessage.getBytes(); // Convert to binary representation
    }

    /**
     * Creates a new Message from a byte array input.
     * This constructor converts the byte array to a string and delegates to the string constructor.
     * 
     * @param newMessage The binary data to create the message from
     */
    public Message(byte[] newMessage)  {
        this(new String(newMessage));
    }

    /**
     * Creates a new Message from a numeric input.
     * This constructor converts the double to its string representation and delegates 
     * to the string constructor. The asDouble field will contain the original numeric value.
     * 
     * @param newMessage The numeric value to create the message from
     */
    public Message(double newMessage) {
        this(Double.toString(newMessage));
    }
}
