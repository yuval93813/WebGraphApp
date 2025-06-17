package server;

import java.io.*;
import java.util.*;

/**
 * RequestParser is a utility class for parsing HTTP requests from client connections.
 * It provides static methods to parse incoming HTTP request streams and extract
 * all relevant information including headers, parameters, URI segments, and content.
 * 
 * <p>The parser supports standard HTTP methods (GET, POST, DELETE) and handles:</p>
 * <ul>
 * <li>Request line parsing (method, URI, protocol version)</li>
 * <li>URL parameter extraction from query strings</li>
 * <li>HTTP header parsing</li>
 * <li>Request body content extraction</li>
 * <li>URI segmentation for path-based routing</li>
 * </ul>
 * 
 * <p>Example usage:</p>
 * <pre>
 * {@code
 * BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
 * RequestInfo requestInfo = RequestParser.parseRequest(reader);
 * String method = requestInfo.getHttpCommand();
 * String uri = requestInfo.getUri();
 * }
 * </pre>
 * 
 * @author Almog Sharoni Yuval Harary
 * @version 1.0
 * @since 1.0
 */
public class RequestParser {
    
    /**
     * Parses an HTTP request from a BufferedReader and extracts all request components.
     * This method performs comprehensive parsing of the HTTP request including:
     * <ul>
     * <li>Request line parsing to extract HTTP method, URI, and protocol</li>
     * <li>Query parameter extraction from URI</li>
     * <li>HTTP header parsing</li>
     * <li>Request body content extraction based on Content-Length</li>
     * <li>URI path segmentation for routing purposes</li>
     * </ul>
     * 
     * <p>The method handles both GET requests with query parameters and POST requests
     * with form data or content bodies. It supports proper Content-Length handling
     * and includes debugging output for troubleshooting connection issues.</p>
     * 
     * @param reader a BufferedReader connected to the client's input stream
     * @return a RequestInfo object containing all parsed request components
     * @throws IOException if the request is malformed, connection fails, or I/O errors occur
     * @throws NumberFormatException if Content-Length header contains invalid numeric value
     * @see RequestInfo
     */
    public static RequestInfo parseRequest(BufferedReader reader) throws IOException {
        String requestLine = reader.readLine();
        
        if (requestLine == null || requestLine.isEmpty()) {
            System.err.println("[DEBUG] Received null or empty request line");
            throw new IOException("Empty request line");
        }
        
        System.err.println("[DEBUG] Request line: " + requestLine);

        String[] requestParts = requestLine.split(" ");
        
        if (requestParts.length < 3) {
            throw new IOException("Invalid request line: " + requestLine);
        }

        // Initialize HTTP command (e.g POST / GET / DELETE)
        String httpCommand = requestParts[0];
        // Initialize URI 
        String uri = requestParts[1];
        // Initialize URI segments
        String[] uriSegments = uri.split("/");
        // Initialize parameters
        Map<String, String> parameters = new HashMap<>();
        // Initialize header
        Map<String, String> headers = new HashMap<>();
        // Initialize content
        byte[] content = new byte[0];

        // Parse URI and parameters
        int queryIndex = uri.indexOf("?");
        
        // If there are parameters
        if (queryIndex != -1) {
            String queryString = uri.substring(queryIndex + 1);
            String parseUri = uri.substring(0, queryIndex);
            // Update URI segments
            uriSegments = parseUri.split("/");
            
            // Parse parameters
            parameters = parseParameters(queryString, "&");
        }
        
        uriSegments = removeEmptySegments(uriSegments);
        
        // Read headers, additional parameters, and content
        StringBuilder headerReader = new StringBuilder();
        int contentLength = 0;
        
        String line;

        // Parse header section
        while (reader.ready()) {
            // Read line and append to string builder
            line = reader.readLine();
            headerReader.append(line).append("\n");
            
            // If reach the first '\n' - break
            if (line.isEmpty()) {
                break;
            }
            
            // Parse header
            String[] headerParts = line.split(":", 2);
            
            if (headerParts.length == 2) {
                String headerName = headerParts[0].trim().toLowerCase();
                String headerValue = headerParts[1].trim();
                
                headers.put(headerName, headerValue);
                
                if (headerName.equals("content-length")) {
                    contentLength = Integer.parseInt(headerValue);
                }
            }
        }

        StringBuilder parametersReader = new StringBuilder();
        StringBuilder contentReader = new StringBuilder();
        
        if(contentLength == 0) {
            if(reader.ready()) {
                // Parameter
                
                // Parse parameter section
                while (reader.ready()) {
                    // Read line and append to string builder
                    line = reader.readLine();
                    parametersReader.append(line);
                    
                    if (line.isEmpty()) {
                        break;
                    } 
                    else {
                        parameters.putAll(parseParameters(line, "\n"));
                    }
                }
            }
        } else {
            // Read all
            StringBuilder firstReader = new StringBuilder();
            StringBuilder secondReader = new StringBuilder();
            
            // Parse first section (headers/parameters)
            while (reader.ready()) {
                // Read line and append to string builder
                line = reader.readLine();
               
                if (line == null || line.isEmpty()) {
                    break;
                } 
                
                firstReader.append(line).append("\n");
            }
                
            // Give a small pause to allow data to arrive if it's delayed
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // Parse second section (content)
            while (reader.ready()) {
                // Read line and append to string builder
                line = reader.readLine();
                
                if (line == null || line.isEmpty()) {
                    break;
                } 
                
                secondReader.append(line).append("\n");
            }
            
            // Handle case where both readers are empty (likely a connection issue or malformed request)
            if(secondReader.length() == 0 && firstReader.length() == 0) {
                System.err.println("[DEBUG] firstReader: '" + firstReader.toString() + "'");
                System.err.println("[DEBUG] secondReader: '" + secondReader.toString() + "'");
                System.err.println("[DEBUG] Possible connection issue or malformed request - ignoring");
                // Instead of throwing an exception, return a minimal valid request
                return new RequestInfo(httpCommand, uri, uriSegments, parameters, content);
            }
            
            else if(secondReader.length() == 0) {
                contentReader = firstReader;
            }
            else {
                String[] lines = firstReader.toString().split("\n");
                
                for(String parameterLine : lines)
                    parameters.putAll(parseParameters(parameterLine, "\n"));
            
                contentReader = secondReader;
            }
        }
        
        // Convert content to bytes
        content = contentReader.toString().getBytes();
        
        return new RequestInfo(httpCommand, uri, uriSegments, parameters, content);
    }

    /**
     * Parses a parameter string and extracts key-value pairs.
     * This method splits the input string by the specified delimiter and extracts
     * parameter pairs in the format "key=value" or standalone keys.
     * 
     * <p>Examples:</p>
     * <ul>
     * <li>"name=John&age=25" with delimiter "&" → {name: "John", age: "25"}</li>
     * <li>"param1=value1\nparam2=value2" with delimiter "\n" → {param1: "value1", param2: "value2"}</li>
     * <li>"flag&param=value" with delimiter "&" → {flag: "", param: "value"}</li>
     * </ul>
     * 
     * @param queryString the string containing parameters to parse
     * @param delimiter the delimiter used to separate parameter pairs
     * @return a Map containing parsed key-value pairs, with empty strings for keys without values
     * @throws NullPointerException if queryString or delimiter is null
     */
    private static Map<String, String> parseParameters(String queryString, String delimiter) {
        // Initialize Map
        Map<String, String> parameters = new HashMap<>();
        // Split by delimiter
        String[] pairs = queryString.split(delimiter);
        
        for (String pair : pairs) {
            // Split by =
            String[] keyValue = pair.split("=");
            
            // Insert into map
            if (keyValue.length == 2) {
                parameters.put(keyValue[0], keyValue[1]);
            } 
            else if (keyValue.length == 1) {
                parameters.put(keyValue[0], "");
            }
        }
        return parameters;
    }

    /**
     * Removes empty or null segments from a URI segment array.
     * This utility method filters out empty strings and null values from URI path segments,
     * which commonly occur when splitting URIs that start with "/" or contain consecutive slashes.
     * 
     * <p>Example: ["/", "api", "", "users", null] → ["api", "users"]</p>
     * 
     * @param segments the array of URI segments to filter
     * @return a new array containing only non-empty, non-null segments
     * @throws NullPointerException if the segments array is null
     */
    private static String[] removeEmptySegments(String[] segments) {
        return Arrays.stream(segments)
                               .filter(segment -> segment != null && !segment.isEmpty())
                               .toArray(String[]::new);
    }
    
    /**
     * RequestInfo is an immutable data class that encapsulates all components of a parsed HTTP request.
     * This inner class provides structured access to request data including the HTTP method,
     * URI, path segments, parameters, and request body content.
     * 
     * <p>Instances of this class are created by the RequestParser.parseRequest() method and
     * provide read-only access to the parsed request components through getter methods.</p>
     * 
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * RequestInfo info = RequestParser.parseRequest(reader);
     * String method = info.getHttpCommand();        // "GET", "POST", etc.
     * String fullUri = info.getUri();               // "/api/users?id=123"
     * String[] pathSegments = info.getUriSegments(); // ["api", "users"]
     * Map<String, String> params = info.getParameters(); // {id: "123"}
     * byte[] body = info.getContent();              // Request body as bytes
     * }
     * </pre>
     * 
     * @author Your Name
     * @version 1.0
     * @since 1.0
     */
    public static class RequestInfo {
        
        /** The HTTP method/command (GET, POST, DELETE, etc.) */
        private final String httpCommand;
        
        /** The complete URI including query parameters */
        private final String uri;
        
        /** The URI path split into segments for routing */
        private final String[] uriSegments;
        
        /** Map of parsed query parameters and form data */
        private final Map<String, String> parameters;
        
        /** The request body content as raw bytes */
        private final byte[] content;

        /**
         * Constructs a new RequestInfo with the specified request components.
         * This constructor creates an immutable request information object with all
         * the parsed components of an HTTP request.
         * 
         * @param httpCommand the HTTP method (e.g., "GET", "POST", "DELETE")
         * @param uri the complete URI including any query parameters
         * @param uriSegments the URI path split into individual segments
         * @param parameters a map of parsed query parameters and form data
         * @param content the request body content as a byte array
         */
        public RequestInfo(String httpCommand, String uri, String[] uriSegments, Map<String, String> parameters, byte[] content) {
            this.httpCommand = httpCommand;
            this.uri = uri;
            this.uriSegments = uriSegments;
            this.parameters = parameters;
            this.content = content;
        }

        /**
         * Returns the HTTP method/command for this request.
         * 
         * @return the HTTP method as a string (e.g., "GET", "POST", "DELETE")
         */
        public String getHttpCommand() {
            return httpCommand;
        }

        /**
         * Returns the complete URI for this request.
         * This includes the path and any query parameters.
         * 
         * @return the complete URI string (e.g., "/api/users?id=123")
         */
        public String getUri() {
            return uri;
        }

        /**
         * Returns the URI path segments for routing purposes.
         * The URI path is split into individual segments with empty segments removed.
         * 
         * @return an array of URI path segments (e.g., ["api", "users"] for "/api/users")
         */
        public String[] getUriSegments() {
            return uriSegments;
        }

        /**
         * Returns the parsed request parameters.
         * This includes both query parameters from the URI and form data from POST requests.
         * 
         * @return a Map containing parameter names as keys and their values as strings
         */
        public Map<String, String> getParameters() {
            return parameters;
        }

        /**
         * Returns the request body content as raw bytes.
         * For GET requests, this is typically an empty byte array.
         * For POST requests, this contains the request body data.
         * 
         * @return the request body content as a byte array
         */
        public byte[] getContent() {
            return content;
        }
    }
}
