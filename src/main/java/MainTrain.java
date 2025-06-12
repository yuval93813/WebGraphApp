

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
import server.RequestParser;
import server.MyHTTPServer;
import servlets.Servlet;


public class MainTrain { // RequestParser
        private static void graphParseRequest() {
        graphBasicRequestParsing();
        graphDifferentHttpMethods();
        graphRequestWithoutParameters();
        graphRequestWithMultipleParameters();
        graphRequestWithContent();
        graphEmptyRequest();
    }
    
    private static void graphBasicRequestParsing() {
        // Test data
        String request = "GET /api/resource?id=123&name=graph HTTP/1.1\n" +
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
                System.out.println("HTTP command graph failed (-5)");
            }

            // Test URI
            if (!requestInfo.getUri().equals("/api/resource?id=123&name=graph")) {
                System.out.println("URI graph failed (-5)");
            }

            // Test URI segments
            String[] expectedUriSegments = {"api", "resource"};
            if (!Arrays.equals(requestInfo.getUriSegments(), expectedUriSegments)) {
                System.out.println("URI segments graph failed (-5)");
                for(String s : requestInfo.getUriSegments()){
                    System.out.println(s);
                }
            } 
            // Test parameters
            Map<String, String> expectedParams = new HashMap<>();
            expectedParams.put("id", "123");
            expectedParams.put("name", "graph");
            expectedParams.put("filename","\"hello_world.txt\"");
            if (!requestInfo.getParameters().equals(expectedParams)) {
                System.out.println("Parameters graph failed (-5)");
            }

            // Test content
            byte[] expectedContent = "hello world!\n".getBytes();
            if (!Arrays.equals(requestInfo.getContent(), expectedContent)) {
                System.out.println("Content graph failed (-5)");
            } 
            input.close();
        } catch (IOException e) {
            System.out.println("Exception occurred during parsing: " + e.getMessage() + " (-5)");
        }        
    }
    
    private static void graphDifferentHttpMethods() {
        // Test POST request
        String postRequest = "POST /api/users HTTP/1.1\n" +
                           "Host: example.com\n" +
                           "Content-Type: application/json\n" +
                           "\n";

        BufferedReader input = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(postRequest.getBytes())));
        try {
            RequestParser.RequestInfo requestInfo = RequestParser.parseRequest(input);
            if (!requestInfo.getHttpCommand().equals("POST")) {
                System.out.println("POST method graph failed (-3)");
            }
            if (!requestInfo.getUri().equals("/api/users")) {
                System.out.println("POST URI graph failed (-3)");
            }
            input.close();
        } catch (IOException e) {
            System.out.println("POST request parsing failed (-3)");
        }
        
        // Test DELETE request
        String deleteRequest = "DELETE /api/users/123 HTTP/1.1\n" +
                             "Host: example.com\n" +
                             "\n";

        input = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(deleteRequest.getBytes())));
        try {
            RequestParser.RequestInfo requestInfo = RequestParser.parseRequest(input);
            if (!requestInfo.getHttpCommand().equals("DELETE")) {
                System.out.println("DELETE method graph failed (-3)");
            }
            String[] expectedSegments = {"api", "users", "123"};
            if (!Arrays.equals(requestInfo.getUriSegments(), expectedSegments)) {
                System.out.println("DELETE URI segments graph failed (-3)");
            }
            input.close();
        } catch (IOException e) {
            System.out.println("DELETE request parsing failed (-3)");
        }
    }
    
    private static void graphRequestWithoutParameters() {
        String request = "GET /simple/path HTTP/1.1\n" +
                        "Host: example.com\n" +
                        "\n";

        BufferedReader input = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(request.getBytes())));
        try {
            RequestParser.RequestInfo requestInfo = RequestParser.parseRequest(input);
            if (!requestInfo.getUri().equals("/simple/path")) {
                System.out.println("Simple URI graph failed (-2)");
            }
            if (!requestInfo.getParameters().isEmpty()) {
                System.out.println("Empty parameters graph failed (-2)");
            }
            String[] expectedSegments = {"simple", "path"};
            if (!Arrays.equals(requestInfo.getUriSegments(), expectedSegments)) {
                System.out.println("Simple URI segments graph failed (-2)");
            }
            input.close();
        } catch (IOException e) {
            System.out.println("Simple request parsing failed (-2)");
        }
    }
    
    private static void graphRequestWithMultipleParameters() {
        String request = "GET /search?q=java&category=programming&sort=date&limit=10 HTTP/1.1\n" +
                        "Host: example.com\n" +
                        "\n";

        BufferedReader input = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(request.getBytes())));
        try {
            RequestParser.RequestInfo requestInfo = RequestParser.parseRequest(input);
            Map<String, String> params = requestInfo.getParameters();
            
            if (!params.get("q").equals("java")) {
                System.out.println("Multiple params 'q' graph failed (-2)");
            }
            if (!params.get("category").equals("programming")) {
                System.out.println("Multiple params 'category' graph failed (-2)");
            }
            if (!params.get("sort").equals("date")) {
                System.out.println("Multiple params 'sort' graph failed (-2)");
            }
            if (!params.get("limit").equals("10")) {
                System.out.println("Multiple params 'limit' graph failed (-2)");
            }
            input.close();
        } catch (IOException e) {
            System.out.println("Multiple parameters parsing failed (-2)");
        }
    }
    
    private static void graphRequestWithContent() {
        String request = "POST /api/data HTTP/1.1\n" +
                        "Host: example.com\n" +
                        "Content-Length: 25\n" +
                        "\n" +
                        "\n" +
                        "This is the request body\n" +
                        "\n";

        BufferedReader input = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(request.getBytes())));
        try {
            RequestParser.RequestInfo requestInfo = RequestParser.parseRequest(input);
            String content = new String(requestInfo.getContent());
            if (!content.contains("This is the request body")) {
                System.out.println("Request content graph failed (-3)");
            }
            input.close();
        } catch (IOException e) {
            System.out.println("Request with content parsing failed (-3)");
        }
    }
      private static void graphEmptyRequest() {
        String request = "";
        BufferedReader input = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(request.getBytes())));
        try {
            RequestParser.parseRequest(input);
            System.out.println("Empty request should throw exception (-2)");
            input.close();
        } catch (IOException e) {
            // Expected behavior - empty request should throw exception
        }
    }
    
    private static void graphServerErrorHandling() throws Exception {
        MyHTTPServer server = new MyHTTPServer(8086, 5);
        
        // Create a servlet that throws an exception
        Servlet errorServlet = new Servlet() {
            @Override
            public void handle(RequestInfo ri, OutputStream toClient) throws Exception {
                throw new RuntimeException("Test exception");
            }
            
            @Override
            public void close() throws IOException {}
        };
        
        server.addServlet("GET", "/error", errorServlet);
        server.start();
        Thread.sleep(200);
        
        // Test that server doesn't crash when servlet throws exception
        try (Socket client = new Socket("localhost", 8086);
             PrintWriter out = new PrintWriter(client.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()))) {
            
            out.println("GET /error HTTP/1.1");
            out.println("Host: localhost");
            out.println();
            
            // Server should still be responsive after exception
            // We don't expect a clean response here, but server shouldn't crash
        } catch (Exception e) {
            // Expected - connection might fail due to servlet exception
        }
        
        // Test that server is still working with a good servlet
        server.addServlet("GET", "/good", createEchoServlet("Working"));
        
        try (Socket client = new Socket("localhost", 8086);
             PrintWriter out = new PrintWriter(client.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()))) {
            
            out.println("GET /good HTTP/1.1");
            out.println("Host: localhost");
            out.println();
            
            String response = in.readLine();
            if (!response.startsWith("HTTP/1.1 200 OK")) {
                System.out.println("Server recovery after error graph failed (-5)");
            }
        } catch (Exception e) {
            System.out.println("Server not responsive after error (-5)");
        }
        
        server.close();
        Thread.sleep(100);
    }    public static void graphServer() throws Exception{
        graphBasicServerFunctionality();
        graphMultipleHttpMethods();
        graphServletMatching();
        graphConcurrentRequests();
        graph404Responses();
        graphParameterHandling();
        graphServerErrorHandling();
        graphServletRemoval();
    }
    
    private static void graphBasicServerFunctionality() throws Exception {
        MyHTTPServer server = new MyHTTPServer(8080, 5);
        
        // Create a simple graph servlet
        Servlet graphServlet = new Servlet() {
            @Override
            public void handle(RequestInfo ri, OutputStream toClient) throws Exception {
                String response = "HTTP/1.1 200 OK\r\n" +
                                "Content-Type: text/plain\r\n" +
                                "Content-Length: 11\r\n" +
                                "\r\n" +
                                "Hello World";
                toClient.write(response.getBytes());
                toClient.flush();
            }
            
            @Override
            public void close() throws IOException {
                // Nothing to close
            }
        };
        
        server.addServlet("GET", "/graph", graphServlet);
        server.start();
        Thread.sleep(200);
        
        // Test basic GET request
        try (Socket client = new Socket("localhost", 8080);
             PrintWriter out = new PrintWriter(client.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()))) {
            
            out.println("GET /graph HTTP/1.1");
            out.println("Host: localhost");
            out.println();
            
            String responseLine = in.readLine();
            if (!responseLine.startsWith("HTTP/1.1 200 OK")) {
                System.out.println("Basic server graph failed (-10)");
            }
        } catch (Exception e) {
            System.out.println("Basic server connection failed (-10)");
        }
        
        server.close();
        Thread.sleep(100);
    }
    
    private static void graphMultipleHttpMethods() throws Exception {
        MyHTTPServer server = new MyHTTPServer(8081, 5);
        
        // Create servlets for different HTTP methods
        Servlet getServlet = createEchoServlet("GET Response");
        Servlet postServlet = createEchoServlet("POST Response");
        Servlet deleteServlet = createEchoServlet("DELETE Response");
        
        server.addServlet("GET", "/api", getServlet);
        server.addServlet("POST", "/api", postServlet);
        server.addServlet("DELETE", "/api", deleteServlet);
        server.start();
        Thread.sleep(200);
        
        // Test GET
        graphHttpMethod(8081, "GET", "/api", "GET Response");
        
        // Test POST
        graphHttpMethod(8081, "POST", "/api", "POST Response");
        
        // Test DELETE
        graphHttpMethod(8081, "DELETE", "/api", "DELETE Response");
        
        server.close();
        Thread.sleep(100);
    }
    
    private static void graphServletMatching() throws Exception {
        MyHTTPServer server = new MyHTTPServer(8082, 5);
        
        // Test longest prefix matching
        server.addServlet("GET", "/api", createEchoServlet("Short"));
        server.addServlet("GET", "/api/v1", createEchoServlet("Long"));
        server.addServlet("GET", "/api/v1/users", createEchoServlet("Longest"));
        server.start();
        Thread.sleep(200);
        
        // Test that longest prefix wins
        graphHttpMethod(8082, "GET", "/api/v1/users/123", "Longest");
        graphHttpMethod(8082, "GET", "/api/v1/graph", "Long");
        graphHttpMethod(8082, "GET", "/api/graph", "Short");
        
        server.close();
        Thread.sleep(100);
    }
    
    private static void graphConcurrentRequests() throws Exception {
        MyHTTPServer server = new MyHTTPServer(8083, 10);
        
        // Create a servlet that takes some time to respond
        Servlet slowServlet = new Servlet() {
            @Override
            public void handle(RequestInfo ri, OutputStream toClient) throws Exception {
                Thread.sleep(100); // Simulate processing time
                String response = "HTTP/1.1 200 OK\r\n" +
                                "Content-Type: text/plain\r\n" +
                                "Content-Length: 4\r\n" +
                                "\r\n" +
                                "Done";
                toClient.write(response.getBytes());
                toClient.flush();
            }
            
            @Override
            public void close() throws IOException {}
        };
        
        server.addServlet("GET", "/slow", slowServlet);
        server.start();
        Thread.sleep(200);
        
        // Test concurrent requests
        Thread[] threads = new Thread[5];
        boolean[] results = new boolean[5];
        
        for (int i = 0; i < 5; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                try (Socket client = new Socket("localhost", 8083);
                     PrintWriter out = new PrintWriter(client.getOutputStream(), true);
                     BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()))) {
                    
                    out.println("GET /slow HTTP/1.1");
                    out.println("Host: localhost");
                    out.println();
                    
                    String response = in.readLine();
                    results[index] = response != null && response.startsWith("HTTP/1.1 200 OK");
                } catch (Exception e) {
                    results[index] = false;
                }
            });
            threads[i].start();
        }
        
        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }
        
        // Check if all requests succeeded
        for (boolean result : results) {
            if (!result) {
                System.out.println("Concurrent request graph failed (-10)");
                break;
            }
        }
        
        server.close();
        Thread.sleep(100);
    }
    
    private static void graph404Responses() throws Exception {
        MyHTTPServer server = new MyHTTPServer(8084, 5);
        
        server.addServlet("GET", "/exists", createEchoServlet("Found"));
        server.start();
        Thread.sleep(200);
        
        // Test 404 for non-existent path
        try (Socket client = new Socket("localhost", 8084);
             PrintWriter out = new PrintWriter(client.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()))) {
            
            out.println("GET /notfound HTTP/1.1");
            out.println("Host: localhost");
            out.println();
            
            String response = in.readLine();
            if (!response.contains("404")) {
                System.out.println("404 response graph failed (-5)");
            }
        } catch (Exception e) {
            System.out.println("404 graph connection failed (-5)");
        }
        
        server.close();
        Thread.sleep(100);
    }
    
    private static void graphParameterHandling() throws Exception {
        MyHTTPServer server = new MyHTTPServer(8085, 5);
        
        // Create servlet that echoes parameters
        Servlet paramServlet = new Servlet() {
            @Override
            public void handle(RequestInfo ri, OutputStream toClient) throws Exception {
                String params = ri.getParameters().toString();
                String response = "HTTP/1.1 200 OK\r\n" +
                                "Content-Type: text/plain\r\n" +
                                "Content-Length: " + params.length() + "\r\n" +
                                "\r\n" +
                                params;
                toClient.write(response.getBytes());
                toClient.flush();
            }
            
            @Override
            public void close() throws IOException {}
        };
        
        server.addServlet("GET", "/params", paramServlet);
        server.start();
        Thread.sleep(200);
        
        // Test parameter parsing
        try (Socket client = new Socket("localhost", 8085);
             PrintWriter out = new PrintWriter(client.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()))) {
            
            out.println("GET /params?name=graph&id=123 HTTP/1.1");
            out.println("Host: localhost");
            out.println();
            
            String statusLine = in.readLine();
            if (!statusLine.startsWith("HTTP/1.1 200 OK")) {
                System.out.println("Parameter graph status failed (-5)");
            }
            
            // Skip headers
            String line;
            while ((line = in.readLine()) != null && !line.isEmpty()) {
                // Skip headers
            }
            
            // Read response body
            String body = in.readLine();
            if (body == null || (!body.contains("name=graph") || !body.contains("id=123"))) {
                System.out.println("Parameter parsing graph failed (-5)");
            }
        } catch (Exception e) {
            System.out.println("Parameter graph connection failed (-5)");
        }
        
        server.close();
        Thread.sleep(100);
    }
    
    private static void graphServletRemoval() throws Exception {
        MyHTTPServer server = new MyHTTPServer(8087, 5);
        
        Servlet graphServlet = createEchoServlet("Present");
        server.addServlet("GET", "/removeme", graphServlet);
        server.start();
        Thread.sleep(200);
        
        // Test that servlet is initially present
        graphHttpMethod(8087, "GET", "/removeme", "Present");
        
        // Remove the servlet
        server.removeServlet("GET", "/removeme");
        
        // Test that servlet is now gone (should get 404)
        try (Socket client = new Socket("localhost", 8087);
             PrintWriter out = new PrintWriter(client.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()))) {
            
            out.println("GET /removeme HTTP/1.1");
            out.println("Host: localhost");
            out.println();
            
            String response = in.readLine();
            if (!response.contains("404")) {
                System.out.println("Servlet removal graph failed (-5)");
            }
        } catch (Exception e) {
            System.out.println("Servlet removal graph connection failed (-5)");
        }
        
        server.close();
        Thread.sleep(100);
    }
    
    private static Servlet createEchoServlet(String message) {
        return new Servlet() {
            @Override
            public void handle(RequestInfo ri, OutputStream toClient) throws Exception {
                String response = "HTTP/1.1 200 OK\r\n" +
                                "Content-Type: text/plain\r\n" +
                                "Content-Length: " + message.length() + "\r\n" +
                                "\r\n" +
                                message;
                toClient.write(response.getBytes());
                toClient.flush();
            }
            
            @Override
            public void close() throws IOException {}
        };
    }
    
    private static void graphHttpMethod(int port, String method, String path, String expectedContent) {
        try (Socket client = new Socket("localhost", port);
             PrintWriter out = new PrintWriter(client.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()))) {
            
            out.println(method + " " + path + " HTTP/1.1");
            out.println("Host: localhost");
            out.println();
            
            String statusLine = in.readLine();
            if (!statusLine.startsWith("HTTP/1.1 200 OK")) {
                System.out.println(method + " method graph failed (-5)");
                return;
            }
            
            // Skip headers
            String line;
            while ((line = in.readLine()) != null && !line.isEmpty()) {
                // Skip headers
            }
            
            // Read response body
            String body = in.readLine();
            if (!expectedContent.equals(body)) {
                System.out.println(method + " response content graph failed (-5)");
            }
        } catch (Exception e) {
            System.out.println(method + " method connection failed (-5)");
        }
    }
    
    public static void main(String[] args) {
        graphParseRequest(); // 40 points
        try{
            graphServer(); // 60
        }catch(Exception e){
            System.out.println("your server throwed an exception (-60)");
        }
        System.out.println("done");
    }

}
