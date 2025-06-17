package server;

import server.RequestParser.RequestInfo;
import servlets.Servlet;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class MyHTTPServer extends Thread implements HTTPServer{
    
    // Data Members
    private int port;
    private int numberOfThreads = 0;
    private ServerSocket serverSocket;
    private ExecutorService threadPool;
    
    private ConcurrentHashMap<String, Servlet> getHttpCommandMap = new ConcurrentHashMap<String, Servlet>();
    private ConcurrentHashMap<String, Servlet> postHttpCommandMap = new ConcurrentHashMap<String, Servlet>();
    private ConcurrentHashMap<String, Servlet> deleteHttpCommandMap = new ConcurrentHashMap<String, Servlet>();
    
    // Constructor
    public MyHTTPServer(int port, int nThreads){
        this.port = port;
        this.numberOfThreads = nThreads;
    }

    // Add servlet into the map
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

    // Remove servlet from the map
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
    }    // Run server method (apply by start)
    public void run(){
        try {            // Redirect System.err to a log file
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

    // Close server method
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
    
    // Close servlets
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
    }    // Handle client request
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

    // Return the match servlet to the given uri
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
