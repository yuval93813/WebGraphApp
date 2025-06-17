/*  
    Advanced Programming EX 1
    Name & ID: Yuval Harary 315112367
*/
import graph.*;
import java.util.Random;

public class MyTestsEX1 {

    // Custom Agent1 implementation
    public static class Agent1 implements Agent {
        double sum = 0;
        int count = 0;

        @Override
        public String getName() {
            return "Agent1";
        }

        @Override
        public void reset() {
            sum = 0;
            count = 0;
        }

        @Override
        public void callback(String topic, Message msg) {
            count++;
            sum += msg.asDouble;

           
        }

        @Override
        public void close() {
            System.out.println("Agent1 closed.");
        }
    }

    // Custom Agent2 implementation
    public static class Agent2 implements Agent {
        double sum = 0;

        @Override
        public String getName() {
            return "Agent2";
        }

        @Override
        public void reset() {
            sum = 0;
        }

        @Override
        public void callback(String topic, Message msg) {
            sum = msg.asDouble; // Update sum with the latest message value
        }

        @Override
        public void close() {
            System.out.println("Agent2 closed.");
        }

        public double getSum() {
            return sum;
        }
    }

    public static void main(String[] args) {
        // Initialize TopicManager
        TopicManagerSingleton.TopicManager tm = TopicManagerSingleton.get();

        // Test 1: Basic subscription and message publishing
        System.out.println("Test 1: Basic subscription and message publishing");
        Topic numbersTopic = tm.getTopic("Numbers");
        Agent1 agent1 = new Agent1();
        Agent2 agent2 = new Agent2();
        numbersTopic.subscribe(agent1);
        numbersTopic.subscribe(agent2);
        numbersTopic.publish(new Message(42));
        if (agent1.sum == 42 && agent2.getSum() == 42) {
            System.out.println("Test 1 successful!");
        } else {
            System.out.println("Test 1 failed!");
        }

        // Test 2: Publishing multiple messages
        System.out.println("\nTest 2: Publishing multiple messages");
        for (int i = 1; i <= 5; i++) {
            numbersTopic.publish(new Message(i * 10));
            // System.out.println("After publishing " + (i * 10) + ":");
            // System.out.println("Agent1 sum: " + agent1.sum);
            // System.out.println("Agent2 sum: " + agent2.getSum());
        }
        if (agent1.sum == 42 + 10 + 20 + 30 + 40 + 50 && agent2.getSum() == 50) {
            System.out.println("Test 2 successful!");
        } else {
            System.out.println("Test 2 failed!");
            System.out.println("Expected Agent1 sum: " + (42 + 10 + 20 + 30 + 40 + 50));
            System.out.println("Actual Agent1 sum: " + agent1.sum);
            System.out.println("Expected Agent2 sum: 50");
            System.out.println("Actual Agent2 sum: " + agent2.getSum());
        }

        // Test 3: Unsubscribing an agent
        System.out.println("\nTest 3: Unsubscribing an agent");
        numbersTopic.unsubscribe(agent1);
        numbersTopic.publish(new Message(100));

        // Check if agent1's sum remains unchanged
        boolean agent1Unchanged = agent1.sum == 42 + 10 + 20 + 30 + 40 + 50;

        // Check if agent2 correctly processes the new message
        boolean agent2Updated = agent2.getSum() == 100;

        if (agent1Unchanged && agent2Updated) {
            System.out.println("Test 3 successful!");
        } else {
            System.out.println("Test 3 failed!");
            System.out.println("Agent1 sum: " + agent1.sum);
            System.out.println("Agent2 sum: " + agent2.getSum());
        }

        // Test 4: Edge case - Publishing a message with NaN
        System.out.println("\nTest 4: Edge case - Publishing a message with NaN");
        numbersTopic.publish(new Message(Double.NaN));
        if (Double.isNaN(agent2.getSum())) {
            System.out.println("Test 4 successful!");
        } else {
            System.out.println("Test 4 failed!");
        }

        // Test 5: Edge case - Publishing a message with an empty string NaN case
        System.out.println("\nTest 5: Edge case - Publishing a message with an empty string");
        numbersTopic.publish(new Message(""));
        if (agent1.sum == 42 + 10 + 20 + 30 + 40 + 50 && Double.isNaN(agent2.getSum())) {
            System.out.println("Test 5 successful!");
        } else {
            System.out.println("Test 5 failed!");
            System.out.println("Expected Agent1 sum: " + (42 + 10 + 20 + 30 + 40 + 50));
            System.out.println("Actual Agent1 sum: " + agent1.sum);
            System.out.println("Expected Agent2 sum: NaN");
            System.out.println("Actual Agent2 sum: " + agent2.getSum());
        }

        // Test 6: Edge case - Publishing a message with a very large number
        System.out.println("\nTest 6: Edge case - Publishing a message with a very large number");
        numbersTopic.publish(new Message(Double.MAX_VALUE));
        if (agent2.getSum() == Double.MAX_VALUE) {
            System.out.println("Test 6 successful!");
        } else {
            System.out.println("Test 6 failed!");
        }

        // Test 7: Edge case - Publishing a message with a very small number
        System.out.println("\nTest 7: Edge case - Publishing a message with a very small number");
        numbersTopic.publish(new Message(Double.MIN_VALUE));
        if (agent2.getSum() == Double.MIN_VALUE) {
            System.out.println("Test 7 successful!");
        } else {
            System.out.println("Test 7 failed!");
        }

        // Test 8: Multiple topics
        System.out.println("\nTest 8: Multiple topics");
        Topic lettersTopic = tm.getTopic("Letters");
        lettersTopic.subscribe(agent1);
        lettersTopic.publish(new Message("7"));
        lettersTopic.publish(new Message("2"));
        if (agent1.sum == 42 + 10 + 20 + 30 + 40 + 50+7+2) {
            
            System.out.println("Test 8 successful!");
        } else {
            System.out.println("Test 8 failed!");
        }

        // Test 9: Clearing topics
        System.out.println("\nTest 9: Clearing topics");
        tm.clear();
        if (tm.getTopics().size() == 0) {
            System.out.println("Test 9 successful!");
        } else {
            System.out.println("Test 9 failed!");
        }

        // Test 10: Randomized stress test
        System.out.println("\nTest 10: Randomized stress test");
        Random random = new Random();
        for (int i = 0; i < 100; i++) {
            numbersTopic.publish(new Message(random.nextInt(1000)));
        }
        if (agent1.sum > 0 && agent2.getSum() > 0) {
            System.out.println("Test 10 successful!");
        } else {
            System.out.println("Test 10 failed!");
        }

        // Cleanup
        agent1.close();
        agent2.close();
        System.out.println("\nAll tests completed.");
    }
}