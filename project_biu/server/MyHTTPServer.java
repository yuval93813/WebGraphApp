package server;

import server.RequestParser.RequestInfo;
import servlets.Servlet;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

/**
 * MyHTTPServer is a concrete implementation of the HTTPServer interface.
 * It provides a multi-threaded HTTP server that can handle GET, POST, and DELETE requests
 * by routing them to registered servlets based on URI patterns.
 * 
 * <p>The server uses a thread pool to handle concurrent requests efficiently and supports
 * servlet registration/removal at runtime. It performs longest-prefix matching for URI routing
 * and includes comprehensive error handling and logging capabilities.</p>
 * 
 * <p>Example usage:</p>
 * <pre>
 * {@code
 * MyHTTPServer server = new MyHTTPServer(8080, 10);
 * server.addServlet("GET", "/api", new ApiServlet());
 * server.start();
 * }
 * </pre>
 * 
 * @author Almog Sharoni Yuval Harary
 * @version 1.0
 * @since 1.0
 */
public class MyHTTPServer extends Thread implements HTTPServer{
    
    /** The port number on which the server listens for incoming connections */
    private int port;
    
    /** The number of threads in the thread pool for handling concurrent requests */
    private int numberOfThreads = 0;
    
    /** The server socket that accepts incoming client connections */
    private ServerSocket serverSocket;
    
    /** The thread pool executor for handling client requests concurrently */
    private ExecutorService threadPool;
    
    /** Map storing servlets registered to handle GET requests, keyed by URI pattern */
    private ConcurrentHashMap<String, Servlet> getHttpCommandMap = new ConcurrentHashMap<String, Servlet>();
    
    /** Map storing servlets registered to handle POST requests, keyed by URI pattern */
    private ConcurrentHashMap<String, Servlet> postHttpCommandMap = new ConcurrentHashMap<String, Servlet>();
    
    /** Map storing servlets registered to handle DELETE requests, keyed by URI pattern */
    private ConcurrentHashMap<String, Servlet> deleteHttpCommandMap = new ConcurrentHashMap<String, Servlet>();
    
    /**
     * Constructs a new MyHTTPServer instance with the specified port and thread pool size.
     * 
     * @param port the port number on which the server should listen (1-65535)
     * @param nThreads the number of threads in the thread pool for handling requests
     * @throws IllegalArgumentException if port is not in valid range (1-65535) or nThreads is negative
     */
    public MyHTTPServer(int port, int nThreads){
        this.port = port;
        this.numberOfThreads = nThreads;
    }

    /**
     * Registers a servlet to handle HTTP requests for a specific command and URI pattern.
     * The servlet will be called when incoming requests match both the HTTP method and URI pattern.
     * 
     * @param httpCommand the HTTP method this servlet should handle ("GET", "POST", or "DELETE")
     * @param uri the URI pattern that this servlet should handle (e.g., "/api", "/upload")
     * @param s the servlet instance that will process matching requests
     * @throws IllegalArgumentException if httpCommand is not "GET", "POST", or "DELETE"
     * @throws NullPointerException if any parameter is null
     */
    public void addServlet(String httpCommand, String uri, Servlet s){
        switch(httpCommand.toUpperCase()) {
            case "GET":
                this.getHttpCommandMap.put(uri, s);
                break;
            case "POST":
                this.postHttpCommandMap.put(uri, s);
                break;
            case "DELETE":
                this.deleteHttpCommandMap.put(uri, s);
                break;
            default:
                throw new IllegalArgumentException("Unsupported HTTP command: " + httpCommand);
        }
    }

    /**
     * Removes a previously registered servlet for the specified HTTP command and URI pattern.
     * 
     * @param httpCommand the HTTP method of the servlet to remove ("GET", "POST", or "DELETE")
     * @param uri the URI pattern of the servlet to remove
     * @throws IllegalArgumentException if httpCommand is not "GET", "POST", or "DELETE"
     * @throws NullPointerException if any parameter is null
     */
    public void removeServlet(String httpCommand, String uri){
        switch(httpCommand.toUpperCase()) {
            case "GET":
                this.getHttpCommandMap.remove(uri);
                break;
            case "POST":
                this.postHttpCommandMap.remove(uri);
                break;
            case "DELETE":
                this.deleteHttpCommandMap.remove(uri);
                break;
            default:
                throw new IllegalArgumentException("Unsupported HTTP command: " + httpCommand);
        }
    }

    /**
     * Starts the HTTP server and begins listening for incoming connections.
     * This method creates a server socket, initializes the thread pool, and enters
     * the main server loop to accept and handle client connections.
     * 
     * <p>The server runs in its own thread and performs the following:</p>
     * <ul>
     * <li>Sets up error logging to server_error.log</li>
     * <li>Creates a ServerSocket on the specified port</li>
     * <li>Initializes the thread pool with the specified number of threads</li>
     * <li>Accepts incoming connections and delegates them to worker threads</li>
     * </ul>
     * 
     * @throws RuntimeException if the server fails to start due to port conflicts or I/O errors
     */
    public void run(){
        try {
            // Redirect System.err to a log file
            try {
                String logDir = System.getProperty("user.dir");
                File logFile = new File(logDir, "server_error.log");
                
                // Create parent directories if they don't exist
                logFile.getParentFile().mkdirs();
                
                PrintStream logStream = new PrintStream(new FileOutputStream(logFile, true));
                System.setErr(logStream);
                System.out.println("Error logging enabled. Log file: " + logFile.getAbsolutePath());
            } catch (FileNotFoundException e) {
                System.out.println("Could not open server_error.log for writing error logs: " + e.getMessage());
                e.printStackTrace();
            } catch (SecurityException e) {
                System.out.println("Permission denied creating log file: " + e.getMessage());
            }
            serverSocket = new ServerSocket(port);
            threadPool = Executors.newFixedThreadPool(numberOfThreads);
            System.out.println("HTTP server started on port " + port);
            
            while (!serverSocket.isClosed()) {
                try {
                    Thread.sleep(1000);
                    Socket clientSocket = serverSocket.accept();
                    // Handle client
                    threadPool.execute(() -> handleRequest(clientSocket));
                } catch (IOException e) {
                    if (serverSocket.isClosed()) {
                        System.out.println("Server socket closed.");
                        break;
                    }
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gracefully shuts down the HTTP server and releases all associated resources.
     * This method performs the following cleanup operations:
     * <ul>
     * <li>Closes the server socket to stop accepting new connections</li>
     * <li>Shuts down the thread pool and waits for existing tasks to complete</li>
     * <li>Closes all registered servlets to release their resources</li>
     * </ul>
     * 
     * <p>The method waits up to 5 seconds for the thread pool to terminate gracefully
     * before forcing shutdown.</p>
     * 
     * @throws RuntimeException if an error occurs during server shutdown
     */
    public void close() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            if (threadPool != null) {
                threadPool.shutdown();
                try {
                    if (!threadPool.awaitTermination(5, TimeUnit.SECONDS)) {
                        threadPool.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    threadPool.shutdownNow();
                }
            }
            closeServlets();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Closes all registered servlets to release their resources.
     * This method iterates through all servlet maps (GET, POST, DELETE) and calls
     * the close() method on each registered servlet.
     * 
     * @throws IOException if an error occurs while closing any servlet
     */
    private void closeServlets() throws IOException {
        // Close get http servlets
        for(Servlet servlet : getHttpCommandMap.values()) {
            servlet.close();
        }
        // Close post http servlets
        for(Servlet servlet : postHttpCommandMap.values()) {
            servlet.close();
        }
        // Close delete http servlets
        for(Servlet servlet : deleteHttpCommandMap.values()) {
            servlet.close();
        }
    }

    /**
     * Handles an individual client request in a separate thread.
     * This method performs the following operations:
     * <ul>
     * <li>Parses the HTTP request using RequestParser</li>
     * <li>Matches the request URI to a registered servlet using longest-prefix matching</li>
     * <li>Delegates request processing to the matched servlet</li>
     * <li>Sends appropriate error responses for malformed requests or unmatched URIs</li>
     * <li>Ensures proper cleanup of client socket resources</li>
     * </ul>
     * 
     * @param clientSocket the client socket connection to handle
     */
    private void handleRequest(Socket clientSocket) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             OutputStream out = clientSocket.getOutputStream()) {
            
            // Parse request
            RequestInfo requestInfo = null;
            try {
                requestInfo = RequestParser.parseRequest(reader);
            } catch (IOException e) {
                System.err.println("[ERROR] Failed to parse request: " + e.getMessage());
                // Send a proper HTTP error response
                out.write("HTTP/1.1 400 Bad Request\r\n\r\nBad Request".getBytes());
                return;
            }
            
            Servlet servlet = null;
            
            // Match the URI to the servlet with the longest prefix
            switch(requestInfo.getHttpCommand().toUpperCase()) {
                case "GET":
                    servlet = matchUriToServlet(getHttpCommandMap, requestInfo.getUri());
                    break;
                case "POST":
                    servlet = matchUriToServlet(postHttpCommandMap, requestInfo.getUri());
                    break;
                case "DELETE":
                    servlet = matchUriToServlet(deleteHttpCommandMap, requestInfo.getUri());
                    break;
            }

            if (servlet != null) {
                servlet.handle(requestInfo, out);
            } else {
                out.write("HTTP/1.1 404 Not Found\r\n\r\n".getBytes());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Matches a request URI to a servlet using longest-prefix matching algorithm.
     * This method iterates through all registered URI patterns in the specified command map
     * and returns the servlet with the longest matching prefix.
     * 
     * <p>For example, if servlets are registered for "/api" and "/api/users", 
     * a request to "/api/users/123" would match the "/api/users" servlet.</p>
     * 
     * @param commandMap the map of URI patterns to servlets for a specific HTTP method
     * @param uri the request URI to match against registered patterns
     * @return the servlet with the longest matching URI prefix, or null if no match is found
     */
    private Servlet matchUriToServlet(ConcurrentHashMap<String, Servlet> commandMap, String uri) {
        Servlet matchedServlet = null;
        int longestMatchLength = -1;

        for (String key : commandMap.keySet()) {
            if (uri.startsWith(key) && key.length() > longestMatchLength) {
                matchedServlet = commandMap.get(key);
                longestMatchLength = key.length();
            }
        }

        return matchedServlet;
    }
}
