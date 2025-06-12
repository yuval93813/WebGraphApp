package graph;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A Singleton wrapper class that provides global access to the TopicManager instance.
 * 
 * This class implements the Singleton pattern to ensure that only one TopicManager
 * exists throughout the application lifecycle. It serves as a factory and registry
 * for all topics in the publish-subscribe messaging system.
 * 
 * Usage: TopicManagerSingleton.get().getTopic("topicName")
 */
public class TopicManagerSingleton {

    /**
     * The actual TopicManager implementation that handles topic management operations.
     * This inner class contains all the business logic for managing topics in a thread-safe manner.
     */
    public static class TopicManager{

        /**
         * The single instance of TopicManager (Singleton pattern).
         * This instance is created eagerly when the class is first loaded.
         */
        private static final TopicManager instance = new TopicManager();
        
        /**
         * Thread-safe map that stores all topics by their names.
         * Uses ConcurrentHashMap to ensure thread safety in multi-threaded environments
         * where multiple threads might be creating or accessing topics simultaneously.
         */
        private ConcurrentHashMap<String, Topic> map;

        /**
         * Private constructor to prevent external instantiation (Singleton pattern).
         * Initializes the thread-safe map for storing topics.
         */
        private TopicManager(){
            this.map = new ConcurrentHashMap<String, Topic>();
        }

        /**
         * Retrieves an existing topic or creates a new one if it doesn't exist.
         * 
         * This method is thread-safe and uses the ConcurrentHashMap's computeIfAbsent
         * method to ensure that topic creation is atomic. If multiple threads request
         * the same topic simultaneously, only one topic will be created.
         * 
         * @param topicName The name of the topic to retrieve or create
         * @return The Topic object associated with the given name
         */
        public Topic getTopic(String topicName) {
            // Use computeIfAbsent for thread-safe lazy initialization of topics
            return map.computeIfAbsent(topicName, name -> new Topic(name));
        }

        /**
         * Returns a collection of all currently managed topics.
         * 
         * This method provides access to all topics that have been created
         * through this TopicManager. The returned collection is a view of
         * the current topics and may change as new topics are created.
         * 
         * @return Collection containing all currently existing topics
         */
        public Collection<Topic> getTopics() {
            return map.values();
        }

        /**
         * Removes all topics from the manager.
         * 
         * This method clears the internal map, effectively removing all topics
         * from the system. Use with caution as this will affect all subscribers
         * and publishers that reference these topics.
         */
        public void clear(){
            map.clear();
        }
    }

    /**
     * Provides global access to the singleton TopicManager instance.
     * 
     * This is the main entry point for accessing topic management functionality.
     * All topic operations should go through this method to ensure consistency
     * and proper singleton behavior.
     * 
     * @return The singleton TopicManager instance
     */
    public static TopicManager get(){
        return TopicManager.instance;
    }
    
}