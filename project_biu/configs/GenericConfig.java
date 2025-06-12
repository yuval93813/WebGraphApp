package configs;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import graph.ParallelAgent;
import graph.Agent;

/**
 * GenericConfig class that loads agent configurations from a file
 * and creates the corresponding agents wrapped in ParallelAgent instances.
 */
public class GenericConfig implements Config {
    
    private String configFilePath;
    private List<ParallelAgent> agents;
    
    public GenericConfig() {
        this.agents = new ArrayList<>();
    }
    
    /**
     * Sets the configuration file path
     * @param configFile Path to the configuration file
     */
    public void setConfFile(String configFile) {
        this.configFilePath = configFile;
    }
    
    @Override
    public void create() {
        if (configFilePath == null) {
            throw new IllegalStateException("Configuration file path not set");
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(configFilePath))) {
            String line;
            List<String> lines = new ArrayList<>();
            
            // Read all lines from the file
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    lines.add(line);
                }
            }
            
            // Process lines in groups of 3 (agent class, subs, pubs)
            for (int i = 0; i < lines.size(); i += 3) {
                if (i + 2 < lines.size()) {
                    String agentClassName = lines.get(i);
                    String subsStr = lines.get(i + 1);
                    String pubsStr = lines.get(i + 2);
                    
                    // Parse subscription topics
                    String[] subs = subsStr.split(",");
                    for (int j = 0; j < subs.length; j++) {
                        subs[j] = subs[j].trim();
                    }
                    
                    // Parse publication topics
                    String[] pubs = pubsStr.split(",");
                    for (int j = 0; j < pubs.length; j++) {
                        pubs[j] = pubs[j].trim();
                    }
                    
                    // Create agent instance using reflection
                    try {
                        Class<?> agentClass = Class.forName(agentClassName);
                        Constructor<?> constructor = agentClass.getConstructor(String[].class, String[].class);
                        Agent agent = (Agent) constructor.newInstance(subs, pubs);
                        
                        // Wrap the agent in a ParallelAgent
                        ParallelAgent parallelAgent = new ParallelAgent(agent, 100);
                        agents.add(parallelAgent);
                        
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to create agent: " + agentClassName, e);
                    }
                }
            }
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to read configuration file: " + configFilePath, e);
        }
    }
    
    @Override
    public String getName() {
        return "Generic Configuration";
    }
    
    @Override
    public int getVersion() {
        return 1;
    }
    
    @Override
    public void close() {
        // Close all created agents
        for (ParallelAgent agent : agents) {
            agent.close();
        }
        agents.clear();
    }
}
