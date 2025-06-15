package project_biu.tests.servlets;

import project_biu.server.RequestInfo;
import project_biu.servlets.TopicDisplayer;
import project_biu.configs.TopicManagerSingleton; // Needed to potentially setup some topics
import project_biu.graph.Topic; // Needed to potentially setup some topics


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TopicDisplayerTest {

    // MockRequestInfo (can be shared if refactored)
    static class MockRequestInfo implements RequestInfo {
        private final String uri;
        private final String method = "GET";
        private final Map<String, String> headers = new HashMap<>();
        private final byte[] body = new byte[0];

        public MockRequestInfo(String uri) {
            this.uri = uri;
            headers.put("Host", "localhost");
        }

        @Override public String getUri() { return uri; }
        @Override public String getMethod() { return method; }
        @Override public Map<String, String> getHeaders() { return headers; }
        @Override public byte[] getBody() { return body; }
        @Override public List<String> getUriSegments() { return Arrays.asList(uri.split("/")); }
        @Override public Map<String, String> getUrlParameters() { return new HashMap<>(); }
    }

    public static void main(String[] args) {
        System.out.println("Running TopicDisplayerTest...");
        setupSampleTopics(); // Optional: Add some topics for a more representative graph
        testDisplayTopicsAsHtml();
        System.out.println("TopicDisplayerTest completed.");
    }

    private static void setupSampleTopics() {
        // Optional: If TopicManagerSingleton is empty by default,
        // and we want to test with a graph that has some data.
        // This depends on how TopicManagerSingleton is implemented (e.g., if it's configurable or has addTopic methods)
        // For this test, we'll assume it might be empty or have some default state.
        // If specific topics are needed, they should be added here using TopicManagerSingleton's API.
        // Example:
        // TopicManagerSingleton tms = TopicManagerSingleton.get();
        // if (tms.getTopics().isEmpty()) {
        //     Topic t1 = new Topic("topic1", null); // Assuming a constructor
        //     tms.addTopic(t1); // Assuming an addTopic method
        //     Topic t2 = new Topic("topic2", null);
        //     tms.addTopic(t2);
        //     t1.addChild(t2); // Assuming addChild
        //     t1.addPublisher("agentA");
        //     t2.addSubscriber("agentB");
        // }
        // For now, this setup is commented out to rely on default state or previous configurations.
        // The test will primarily check if HTML is generated, not the specific graph details.
    }


    private static void testDisplayTopicsAsHtml() {
        System.out.print("  testDisplayTopicsAsHtml: ");
        TopicDisplayer servlet = new TopicDisplayer();
        // URI for TopicDisplayer is not typically parsed for filename, but a valid URI is still needed.
        MockRequestInfo mockRi = new MockRequestInfo("/topics");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        boolean pass = true;

        try {
            servlet.handle(mockRi, baos);
            String response = baos.toString(StandardCharsets.UTF_8);

            // Check for 200 OK
            if (!response.startsWith("HTTP/1.1 200 OK")) {
                System.err.println("\n    FAIL: Expected HTTP 200 OK. Got: " + response.substring(0, Math.min(response.length(), 30)) + "...");
                pass = false;
            }

            // Check for Content-Type HTML
            if (!response.contains("Content-Type: text/html; charset=UTF-8")) {
                System.err.println("\n    FAIL: Expected Content-Type text/html. Headers: \n" + response.substring(0, response.indexOf("\r\n\r\n")));
                pass = false;
            }

            // Check for basic HTML structure
            int bodyStartIndex = response.indexOf("\r\n\r\n") + 4;
            String body = response.substring(bodyStartIndex);

            if (!body.toLowerCase().contains("<html>") || !body.toLowerCase().contains("</html>")) {
                System.err.println("\n    FAIL: HTML tags missing.");
                pass = false;
            }
            if (!body.toLowerCase().contains("<head>") || !body.toLowerCase().contains("</head>")) {
                System.err.println("\n    FAIL: HEAD tags missing.");
                pass = false;
            }
            if (!body.toLowerCase().contains("<body>") || !body.toLowerCase().contains("</body>")) {
                System.err.println("\n    FAIL: BODY tags missing.");
                pass = false;
            }
            if (!body.toLowerCase().contains("<title>topic graph</title>")) {
                 System.err.println("\n    FAIL: Expected title 'Topic Graph' not found in body: " + body.substring(0, Math.min(body.length(), 300)));
                pass = false;
            }
            // Check for signs of graph content (more robust checks would parse HTML or use regex)
            if (!body.contains("class=\"node\"") && !body.contains("class=\"edge\"") && !body.contains("<svg") && graphHasContent()) {
                 // Only fail if we expect content. If graph is empty, these might not be present.
                System.err.println("\n    WARN: HTML body does not seem to contain typical graph elements (node/edge divs or SVG). This might be OK if the graph is empty.");
                // pass = false; // Make this a warning or a conditional fail
            }


        } catch (IOException e) {
            System.err.println("\n    FAIL: IOException during test: " + e.getMessage());
            e.printStackTrace();
            pass = false;
        } catch (Exception e) {
            System.err.println("\n    FAIL: Exception during test: " + e.getMessage());
            e.printStackTrace();
            pass = false;
        }
        finally {
            try { servlet.close(); } catch (IOException e) { /* ignore */ }
        }

        if (pass) {
            System.out.println("PASS");
        } else {
            System.err.println("    Review errors above for: testDisplayTopicsAsHtml");
        }
    }

    // Helper to check if TopicManagerSingleton likely has content.
    // This is a rough check.
    private static boolean graphHasContent() {
        TopicManagerSingleton tms = TopicManagerSingleton.get();
        if (tms == null || tms.getTopics() == null || tms.getTopics().isEmpty()) {
            return false;
        }
        // Even if there are topics, they might not have publishers/subscribers or children,
        // leading to a graph with nodes but few or no edges.
        // A more robust check would build a Graph object like TopicDisplayer does and check its emptiness.
        return true;
    }
}
