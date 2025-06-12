package graph;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class ParallelAgent implements Agent {
    
    private final Agent agent;
    private final BlockingQueue<MessageTask> queue;
    private final Thread workerThread;
    private volatile boolean running;
    
    // Inner class to hold the message and topic together
    private static class MessageTask {
        final String topic;
        final Message message;
        
        MessageTask(String topic, Message message) {
            this.topic = topic;
            this.message = message;
        }
    }
    
    public ParallelAgent(Agent agent, int capacity) {
        this.agent = agent;
        this.queue = new ArrayBlockingQueue<>(capacity);
        this.running = true;
        
        // Create and start the worker thread
        this.workerThread = new Thread(() -> {
            while (running) {
                try {
                    MessageTask task = queue.take();
                    if (task != null) {
                        agent.callback(task.topic, task.message);
                    }
                } catch (InterruptedException e) {
                    // Thread was interrupted, exit gracefully
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        
        this.workerThread.start();
    }
    
    @Override
    public String getName() {
        return agent.getName();
    }
    
    @Override
    public void reset() {
        agent.reset();
    }
    
    @Override
    public void callback(String topic, Message msg) {
        try {
            queue.put(new MessageTask(topic, msg));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    @Override
    public void close() {
        running = false;
        workerThread.interrupt();
        try {
            workerThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        agent.close();
    }
}
