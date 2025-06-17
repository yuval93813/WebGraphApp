//package test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import server.RequestParser.RequestInfo;
import servlets.*;
import server.*;

    
  


public class MainTrain5 { // RequestParser
    
    
    private static void testParseRequest() {
        // Test data
        String request = "GET /api/resource?id=123&name=test HTTP/1.1\n" +
                            "Host: example.com\n" +
                            "Content-Length: 5\n"+
                            "\n" +
                            "filename=\"hello_world.txt\"\n"+
                            "\n" +
                            "hello world!\n"+
                            "\n" ;

        BufferedReader input=new BufferedReader(new InputStreamReader(new ByteArrayInputStream(request.getBytes())));
        try {
            RequestParser.RequestInfo requestInfo = RequestParser.parseRequest(input);

            // Test HTTP command
            if (!requestInfo.getHttpCommand().equals("GET")) {
                System.out.println("HTTP command test failed (-5)");
            }

            // Test URI
            if (!requestInfo.getUri().equals("/api/resource?id=123&name=test")) {
                System.out.println("URI test failed (-5)");
            }

            // Test URI segments
            String[] expectedUriSegments = {"api", "resource"};
            if (!Arrays.equals(requestInfo.getUriSegments(), expectedUriSegments)) {
                System.out.println("URI segments test failed (-5)");
                for(String s : requestInfo.getUriSegments()){
                    System.out.println(s);
                }
            } 
            // Test parameters
            Map<String, String> expectedParams = new HashMap<>();
            expectedParams.put("id", "123");
            expectedParams.put("name", "test");
            expectedParams.put("filename","\"hello_world.txt\"");
            if (!requestInfo.getParameters().equals(expectedParams)) {
                System.out.println("Parameters test failed (-5)");
            }

            // Test content
            byte[] expectedContent = "hello world!\n".getBytes();
            if (!Arrays.equals(requestInfo.getContent(), expectedContent)) {
                System.out.println("Content test failed (-5)");
            } 
            input.close();
        } catch (IOException e) {
            System.out.println("Exception occurred during parsing: " + e.getMessage() + " (-5)");
        }        
    }


    public static void testServer() throws Exception {
        // Get the initial thread count before starting the server
        int initialThreadCount = Thread.activeCount();

        // Create and start the server
        MyHTTPServer server = new MyHTTPServer(8080, 1); // Single-threaded server
        server.start(); // Start the server in a new thread

        // Wait briefly to allow the server thread to start
        Thread.sleep(500);

        // Get the thread count after starting the server
        int threadCountAfterStart = Thread.activeCount();

        // Verify that only one additional thread was created
        if (threadCountAfterStart != initialThreadCount + 1) {
            System.out.println("Server did not start with exactly one additional thread (-10)");
        } else {
            System.out.println("Server started with exactly one additional thread");
        }

        // Add the TestServlet to the server
        server.addServlet("GET", "api", new TestServlet());

        // Simulate a client request
        Socket clientSocket = new Socket("127.0.0.1", 8080);
        PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
            //BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {

        // Send the HTTP GET request
        writer.println("GET /api/resource?id=123&name=test HTTP/1.1");
        
        writer.println(); // End of headers
        writer.flush();
        server.close();


        

        // Wait for 2 seconds and verify all threads are closed
        Thread.sleep(2000);
        if (Thread.activeCount() > initialThreadCount) {
            System.out.println("Threads did not close properly (-60)");
        } else {
            System.out.println("All threads closed properly");
        }
    }
    
    public static void main(String[] args) {
        testParseRequest(); // 40 points
        try{
            testServer(); // 60
        }catch(Exception e){
            System.out.println("your server throwed an exception (-60)");
        }
        System.out.println("done");
    }

}
