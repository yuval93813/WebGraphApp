package server;

import servlets.Servlet;

/**
 * HTTPServer interface defines the contract for HTTP server implementations.
 * This interface extends Runnable to allow server instances to run in separate threads.
 * 
 * <p>The server manages HTTP requests and routes them to appropriate servlets based on
 * HTTP method and URI patterns. It supports adding and removing servlets dynamically
 * and provides lifecycle management through start and close methods.</p>
 * 
 * @author Almog Sharoni Yuval Harary
 * @version 1.0
 * @since 1.0
 */
public interface HTTPServer extends Runnable {
    
    /**
     * Adds a servlet to handle HTTP requests for a specific command and URI pattern.
     * 
     * @param httpCommand the HTTP method (e.g., "GET", "POST", "PUT", "DELETE")
     * @param uri the URI pattern that this servlet should handle (e.g., "/api/users", "/upload")
     * @param s the servlet instance that will process matching requests
     * @throws IllegalArgumentException if httpCommand or uri is null or empty
     * @throws IllegalStateException if a servlet is already registered for this command/URI combination
     */
    public void addServlet(String httpCommand, String uri, Servlet s);
    
    /**
     * Removes a previously registered servlet for the specified HTTP command and URI.
     * 
     * @param httpCommand the HTTP method of the servlet to remove
     * @param uri the URI pattern of the servlet to remove
     * @throws IllegalArgumentException if httpCommand or uri is null or empty
     * @throws IllegalStateException if no servlet is registered for this command/URI combination
     */
    public void removeServlet(String httpCommand, String uri);
    
    /**
     * Starts the HTTP server and begins listening for incoming connections.
     * This method should be non-blocking and return immediately after starting
     * the server in a background thread.
     * 
     * @throws IllegalStateException if the server is already running
     * @throws RuntimeException if the server fails to start due to port conflicts or other issues
     */
    public void start();
    
    /**
     * Gracefully shuts down the HTTP server and releases all associated resources.
     * This method should stop accepting new connections, complete processing of
     * existing requests, and clean up all server resources.
     * 
     * <p>After calling this method, the server instance should not be reusable
     * and a new instance should be created if server functionality is needed again.</p>
     * 
     * @throws RuntimeException if an error occurs during server shutdown
     */
    public void close();
}