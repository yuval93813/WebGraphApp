package project_biu.tests.servlets;

import project_biu.server.RequestInfo;
import project_biu.servlets.HtmlLoader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HtmlLoaderTest {

    // Re-using a similar MockRequestInfo, could be refactored into a common test utility
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
        System.out.println("Running HtmlLoaderTest...");
        testLoadExistingHtml();
        testHtmlNotFound();
        testInvalidHtmlUriFormat();
        testInvalidHtmlFileName();
        testNonHtmlExtension();
        System.out.println("HtmlLoaderTest completed.");
    }

    private static void runTest(String testName, String uri, String expectedResponseStart, String expectedContentType, byte[] expectedBody) {
        System.out.print("  " + testName + ": ");
        HtmlLoader servlet = new HtmlLoader();
        MockRequestInfo mockRi = new MockRequestInfo(uri);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        boolean pass = true;

        try {
            servlet.handle(mockRi, baos);
            String response = baos.toString(StandardCharsets.UTF_8);

            if (!response.startsWith(expectedResponseStart)) {
                System.err.println("\n    FAIL: Response start mismatch. Expected prefix: '" + expectedResponseStart + "', Got: '" + response.substring(0, Math.min(response.length(), expectedResponseStart.length() + 30)) + "...'");
                pass = false;
            }

            if (expectedContentType != null) {
                // Content-Type for HTML includes charset, so check for "startsWith"
                if (!response.contains("Content-Type: " + expectedContentType)) {
                     System.err.println("\n    FAIL: Content-Type mismatch. Expected to contain: '" + expectedContentType + "'. Response headers: \n" + response.substring(0, response.indexOf("\r\n\r\n")));
                    pass = false;
                }
            }

            if (expectedBody != null) {
                int bodyStartIndex = response.indexOf("\r\n\r\n") + 4;
                String actualBodyString = response.substring(bodyStartIndex);
                // For HTML, it's generally safer to compare strings after normalizing line endings,
                // but for this test, direct byte comparison should work if file and string use UTF-8.
                byte[] actualBodyBytes = actualBodyString.getBytes(StandardCharsets.UTF_8);

                if (!Arrays.equals(actualBodyBytes, expectedBody)) {
                    System.err.println("\n    FAIL: Response body mismatch.");
                    // Printing full HTML can be verbose, so maybe just lengths or a snippet
                    System.err.println("    Expected Length: " + expectedBody.length);
                    System.err.println("    Actual Length:   " + actualBodyBytes.length);
                    // System.err.println("    Expected: " + new String(expectedBody, StandardCharsets.UTF_8));
                    // System.err.println("    Actual:   " + actualBodyString);
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

    private static void testLoadExistingHtml() {
        String fileName = "test_servlet_page.html";
        String uri = "/html/" + fileName;
        String expectedFileContent = "<!DOCTYPE html>\n" +
                                     "<html lang=\"en\">\n" +
                                     "<head>\n" +
                                     "    <meta charset=\"UTF-8\">\n" +
                                     "    <title>Test HTML Page</title>\n" +
                                     "</head>\n" +
                                     "<body>\n" +
                                     "    <h1>Hello from HtmlLoaderTest!</h1>\n" +
                                     "    <p>This is a test page.</p>\n" +
                                     "</body>\n" +
                                     "</html>";
        byte[] expectedBody = expectedFileContent.getBytes(StandardCharsets.UTF_8);
        runTest("testLoadExistingHtml", uri, "HTTP/1.1 200 OK", "text/html; charset=UTF-8", expectedBody);
    }

    private static void testHtmlNotFound() {
        String uri = "/html/nonexistentpage.html";
        runTest("testHtmlNotFound", uri, "HTTP/1.1 404 Not Found", "text/html; charset=UTF-8", null);
    }

    private static void testInvalidHtmlUriFormat() {
        runTest("testInvalidHtmlUriFormat (no_html_prefix)", "/ht_ml/somepage.html", "HTTP/1.1 400 Bad Request", "text/html; charset=UTF-8", null);
        runTest("testInvalidHtmlUriFormat (empty_filename)", "/html/", "HTTP/1.1 400 Bad Request", "text/html; charset=UTF-8", null);
    }

    private static void testInvalidHtmlFileName() {
        runTest("testInvalidHtmlFileName (traversal_attempt)", "/html/../secret.html", "HTTP/1.1 400 Bad Request", "text/html; charset=UTF-8", null);
        runTest("testInvalidHtmlFileName (slash_in_name)", "/html/folder/page.html", "HTTP/1.1 400 Bad Request", "text/html; charset=UTF-8", null);
    }

    private static void testNonHtmlExtension() {
        // Create a dummy file with a non-HTML extension in html_files for this test
        // For now, we assume the servlet rejects based on URI, not actual file content type.
        runTest("testNonHtmlExtension (.txt)", "/html/somefile.txt", "HTTP/1.1 400 Bad Request", "text/html; charset=UTF-8", null);
         runTest("testNonHtmlExtension (.php)", "/html/somefile.php", "HTTP/1.1 400 Bad Request", "text/html; charset=UTF-8", null);
    }
}
