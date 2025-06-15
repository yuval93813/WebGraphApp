package project_biu.tests.servlets;

import project_biu.server.RequestInfo;
import project_biu.servlets.ConfLoader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfLoaderTest {

    // Inner class to mock RequestInfo
    static class MockRequestInfo implements RequestInfo {
        private final String uri;
        private final String method = "GET"; // Assuming GET for ConfLoader
        private final Map<String, String> headers = new HashMap<>();
        private final byte[] body = new byte[0];

        public MockRequestInfo(String uri) {
            this.uri = uri;
            headers.put("Host", "localhost"); // Example header
        }

        @Override
        public String getUri() { return uri; }
        @Override
        public String getMethod() { return method; }
        @Override
        public Map<String, String> getHeaders() { return headers; }
        @Override
        public byte[] getBody() { return body; }
        @Override
        public List<String> getUriSegments() { return Arrays.asList(uri.split("/")); }
        @Override
        public Map<String, String> getUrlParameters() { return new HashMap<>(); }
    }

    public static void main(String[] args) {
        System.out.println("Running ConfLoaderTest...");
        testLoadExistingConf();
        testConfNotFound();
        testInvalidUriFormat();
        testInvalidFileName();
        System.out.println("ConfLoaderTest completed.");
    }

    private static void runTest(String testName, String uri, String expectedResponseStart, String expectedContentType, byte[] expectedBody) {
        System.out.print("  " + testName + ": ");
        ConfLoader servlet = new ConfLoader();
        MockRequestInfo mockRi = new MockRequestInfo(uri);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        boolean pass = true;
        try {
            servlet.handle(mockRi, baos);
            String response = baos.toString(StandardCharsets.UTF_8);

            if (!response.startsWith(expectedResponseStart)) {
                System.err.println("\n    FAIL: Response start mismatch. Expected prefix: '" + expectedResponseStart + "', Got: '" + response.substring(0, Math.min(response.length(), expectedResponseStart.length() + 20)) + "...'");
                pass = false;
            }

            if (expectedContentType != null) {
                if (!response.contains("Content-Type: " + expectedContentType)) {
                    System.err.println("\n    FAIL: Content-Type mismatch. Expected: '" + expectedContentType + "'. Response headers: \n" + response.substring(0, response.indexOf("\r\n\r\n")));
                    pass = false;
                }
            }

            if (expectedBody != null) {
                // Extract body from response
                int bodyStartIndex = response.indexOf("\r\n\r\n") + 4;
                String actualBodyString = response.substring(bodyStartIndex);
                byte[] actualBodyBytes = actualBodyString.getBytes(StandardCharsets.UTF_8); // Re-encode to avoid charset issues with direct string comparison if original bytes were non-UTF8 text.
                                                                                            // For text/plain UTF-8 is fine.

                // Trim trailing newlines for comparison if necessary, as Files.readAllBytes might add one
                // For this test, we'll compare directly.
                if (!Arrays.equals(actualBodyBytes, expectedBody)) {
                    System.err.println("\n    FAIL: Response body mismatch.");
                    System.err.println("    Expected: " + new String(expectedBody, StandardCharsets.UTF_8));
                    System.err.println("    Actual:   " + actualBodyString);
                    pass = false;
                }
            }

        } catch (IOException e) {
            System.err.println("\n    FAIL: IOException during test: " + e.getMessage());
            e.printStackTrace();
            pass = false;
        } finally {
            try { servlet.close(); } catch (IOException e) { /* ignore */ }
        }

        if (pass) {
            System.out.println("PASS");
        } else {
            System.err.println("    Review errors above for: " + testName);
        }
    }

    private static void testLoadExistingConf() {
        String fileName = "test_servlet_conf.txt";
        String uri = "/conf/" + fileName;
        String expectedFileContent = "This is a test configuration file for ConfLoaderTest.\nLine 1\nLine 2\nEnd of test file.";
        byte[] expectedBody = expectedFileContent.getBytes(StandardCharsets.UTF_8);
        runTest("testLoadExistingConf", uri, "HTTP/1.1 200 OK", "text/plain", expectedBody);
    }

    private static void testConfNotFound() {
        String uri = "/conf/nonexistentfile.txt";
        runTest("testConfNotFound", uri, "HTTP/1.1 404 Not Found", "text/plain", null); // Body check not strict for 404 message content
    }

    private static void testInvalidUriFormat() {
        // Test case 1: URI does not start with /conf/
        runTest("testInvalidUriFormat (no_conf_prefix)", "/c_onf/somefile.txt", "HTTP/1.1 400 Bad Request", "text/plain", null);
        // Test case 2: URI is just /conf/
        runTest("testInvalidUriFormat (empty_filename)", "/conf/", "HTTP/1.1 400 Bad Request", "text/plain", null);
    }

    private static void testInvalidFileName() {
        // Test case: URI with directory traversal attempt
        runTest("testInvalidFileName (traversal_attempt)", "/conf/../secret.txt", "HTTP/1.1 400 Bad Request", "text/plain", null);
        // Test case: URI with slash in filename (not allowed by current implementation)
        runTest("testInvalidFileName (slash_in_name)", "/conf/folder/file.txt", "HTTP/1.1 400 Bad Request", "text/plain", null);
    }
}
