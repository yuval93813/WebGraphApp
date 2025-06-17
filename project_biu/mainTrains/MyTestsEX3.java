/*  
    Advanced Programming EX 3
    Name & ID: Yuval Harary 315112367
*/
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import graph.*;

public class MyTestsEX3 {

    public static class TestAgent implements Agent {
        double sum = 0;
        int count = 0;

        @Override
        public String getName() {
            return "TestAgent";
        }

        @Override
        public void reset() {
            sum = 0;
            count = 0;
        }

        @Override
        public void callback(String topic, Message msg) {
            count++;
            if (!Double.isNaN(msg.asDouble)) { // Ignore NaN values
                sum += msg.asDouble;
            }
        }

        @Override
        public void close() {
            System.out.println("TestAgent closed.");
        }
    }

    public static void main(String[] args) {
        System.out.println("Running ParallelAgent Edge Case Tests...");

        // Test 1: Basic functionality
        System.out.println("\nTest 1: Basic functionality");
        TestAgent testAgent = new TestAgent();
        ParallelAgent parallelAgent = new ParallelAgent(testAgent, 10);

        parallelAgent.callback("Topic1", new Message(10));
        parallelAgent.callback("Topic1", new Message(20));
        parallelAgent.callback("Topic1", new Message(30));

        try {
            Thread.sleep(100); // Allow time for processing
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        if (testAgent.sum == 10 + 20 + 30) {
            System.out.println("Test 1 successful!");
        } else {
            System.out.println("Test 1 failed!");
            System.out.println("Expected sum: 60");
            System.out.println("Actual sum: " + testAgent.sum);
        }

        // Test 2: Empty message
        System.out.println("\nTest 2: Empty message");
        parallelAgent.callback("Topic1", new Message(""));
        try {
            Thread.sleep(100); // Allow time for processing
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        if (testAgent.sum == 10 + 20 + 30) {
            System.out.println("Test 2 successful!");
        } else {
            System.out.println("Test 2 failed!");
            System.out.println("Expected sum: 60");
            System.out.println("Actual sum: " + testAgent.sum);
        }

        // Test 3: High concurrency
        System.out.println("\nTest 3: High concurrency");
        int numMessages = 1000;
        CountDownLatch latch = new CountDownLatch(numMessages);
        for (int i = 0; i < numMessages; i++) {
            int value = i;
            new Thread(() -> {
                parallelAgent.callback("Topic1", new Message(value));
                latch.countDown();
            }).start();
        }

        try {
            latch.await(5, TimeUnit.SECONDS); // Wait for all threads to finish
            Thread.sleep(100); // Allow time for processing
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        double expectedSum = 10 + 20 + 30 + (numMessages * (numMessages - 1)) / 2; // Sum of 0 to 999
        if (testAgent.sum == expectedSum) {
            System.out.println("Test 3 successful!");
        } else {
            System.out.println("Test 3 failed!");
            System.out.println("Expected sum: " + expectedSum);
            System.out.println("Actual sum: " + testAgent.sum);
        }

        // Test 4: Graceful shutdown
        System.out.println("\nTest 4: Graceful shutdown");
        parallelAgent.close();
        System.out.println("ParallelAgent closed successfully.");

        // Test 5: Shutdown while processing
        System.out.println("\nTest 5: Shutdown while processing");
        ParallelAgent parallelAgent2 = new ParallelAgent(testAgent, 10);
        for (int i = 0; i < 10; i++) {
            parallelAgent2.callback("Topic1", new Message(i));
        }
        parallelAgent2.close();
        System.out.println("ParallelAgent closed successfully during processing.");

        // Test 6: Large capacity
        System.out.println("\nTest 6: Large capacity");
        ParallelAgent parallelAgent3 = new ParallelAgent(testAgent, 10000);
        for (int i = 0; i < 10000; i++) {
            parallelAgent3.callback("Topic1", new Message(i));
        }
        parallelAgent3.close();
        System.out.println("ParallelAgent handled large capacity successfully.");

        // Test 7: Edge case - Invalid numeric message
        System.out.println("\nTest 7: Edge case - Invalid numeric message");

        // Reset the testAgent to ensure a clean state
        testAgent.reset();
        System.out.println("Sum before invalid message: " + testAgent.sum);

        // Send an invalid numeric message
        parallelAgent3.callback("Topic1", new Message("abc"));
        System.out.println("Sum after invalid message: " + testAgent.sum);

        try {
            Thread.sleep(100); // Allow time for processing
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Validate the sum remains unchanged
        if (testAgent.sum == 0) { // Expecting 0 because the agent was reset
            System.out.println("Test 7 successful!");
        } else {
            System.out.println("Test 7 failed!");
            System.out.println("Expected sum: 0");
            System.out.println("Actual sum: " + testAgent.sum);
        }

        System.out.println("\nAll tests completed.");
    }
}