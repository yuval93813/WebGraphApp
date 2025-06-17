/*  
    Advanced Programming EX 4
    Name & ID: Yuval Harary 315112367
*/
import graph.*;
import configs.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class MyTestsEX4 {

    public static void main(String[] args) {
        System.out.println("Running MyTestsEX4...");

        testPlusAgentBasicFunctionality();
        testIncAgentBasicFunctionality();
        testGenericConfigEdgeCases();

        System.out.println("\nAll tests completed.");
    }

    // Test 1: Basic functionality of PlusAgent
    public static void testPlusAgentBasicFunctionality() {
        System.out.println("\nTest 1: PlusAgent Basic Functionality");

        String[] subs = {"InputA", "InputB"};
        String[] pubs = {"OutputC"};
        PlusAgent plusAgent = new PlusAgent(subs, pubs);

        // Subscribe to the output topic to observe the result
        GetAgent resultAgent = new GetAgent("OutputC");

        // Publish values to the input topics
        TopicManagerSingleton.get().getTopic("InputA").publish(new Message(5.0));
        TopicManagerSingleton.get().getTopic("InputB").publish(new Message(3.0));

        try {
            Thread.sleep(100); // Allow time for processing
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Validate the result
        if (resultAgent.msg != null && resultAgent.msg.asDouble == 8.0) {
            System.out.println("Test 1 successful!");
        } else {
            System.out.println("Test 1 failed!");
            System.out.println("Expected result: 8.0");
            System.out.println("Actual result: " + (resultAgent.msg != null ? resultAgent.msg.asDouble : "null"));
        }
    }

    // Test 2: Basic functionality of IncAgent
    public static void testIncAgentBasicFunctionality() {
        System.out.println("\nTest 2: IncAgent Basic Functionality");

        String[] subs = {"InputX"};
        String[] pubs = {"OutputY"};
        IncAgent incAgent = new IncAgent(subs, pubs);

        // Subscribe to the output topic to observe the result
        GetAgent resultAgent = new GetAgent("OutputY");

        // Publish a value to the input topic
        TopicManagerSingleton.get().getTopic("InputX").publish(new Message(10.0));

        try {
            Thread.sleep(100); // Allow time for processing
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Validate the result
        if (resultAgent.msg != null && resultAgent.msg.asDouble == 11.0) {
            System.out.println("Test 2 successful!");
        } else {
            System.out.println("Test 2 failed!");
            System.out.println("Expected result: 11.0");
            System.out.println("Actual result: " + (resultAgent.msg != null ? resultAgent.msg.asDouble : "null"));
        }
    }

    // Helper method to count application-specific threads
    private static long countApplicationThreads() {
        return Thread.getAllStackTraces().keySet().stream()
            .filter(thread -> thread.getName().startsWith("Thread-")) // Filter threads by name
            .count();
    }

    // Test 3: Edge cases for GenericConfig
    public static void testGenericConfigEdgeCases() {
        System.out.println("\nTest 3: GenericConfig Edge Cases");

        GenericConfig gc = new GenericConfig();
        gc.setConfFile("config_files\\simple.conf"); // Ensure this file exists and is correctly formatted
        gc.create();

        // Validate that threads are created
        long appThreadsBefore = countApplicationThreads();
        gc.create();
        long appThreadsAfter = countApplicationThreads();

        if (appThreadsAfter > appThreadsBefore) {
            System.out.println("Test 3 successful! Threads created as expected.");
        } else {
            System.out.println("Test 3 failed! No threads were created.");
        }

        // Close the configuration and validate that threads are closed
        gc.close();

        try {
            Thread.sleep(500); // Allow more time for threads to close
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Validate application-specific thread count
        long appThreadsAfterClosing = countApplicationThreads();
        if (appThreadsAfterClosing == 0) { // Expect 0 threads after closing
            System.out.println("Test 3 successful! Threads closed as expected.");
        } else {
            System.out.println("Test 3 failed! Threads were not closed properly.");
            System.out.println("Expected application threads: 0");
            System.out.println("Actual application threads: " + appThreadsAfterClosing);
        }
    }

    // Helper class to observe messages on a topic
    public static class GetAgent implements Agent {
        public Message msg;

        public GetAgent(String topic) {
            TopicManagerSingleton.get().getTopic(topic).subscribe(this);
        }

        @Override
        public String getName() {
            return "GetAgent";
        }

        @Override
        public void reset() {}

        @Override
        public void callback(String topic, Message msg) {
            this.msg = msg;
        }

        @Override
        public void close() {}
    }
}