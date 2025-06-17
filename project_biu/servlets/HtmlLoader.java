package servlets;

import java.io.*;
import server.RequestParser.RequestInfo;

public class HtmlLoader implements Servlet {
    private final String htmlDir;

    public HtmlLoader(String htmlDir) {
        this.htmlDir = htmlDir;
    }

    @Override
    public void handle(RequestInfo ri, OutputStream toClient) throws Exception {
        // Extract requested file path from URI
        String uri = ri.getUri();
        String fileName = uri.substring(uri.lastIndexOf('/') + 1);
        // Remove query string if present
        int qIdx = fileName.indexOf('?');
        if (qIdx != -1) fileName = fileName.substring(0, qIdx);
        if (fileName.isEmpty() || fileName.equals("app")) fileName = "index.html";
        File file = new File(htmlDir, fileName);
        if (!file.exists() || file.isDirectory()) {
            // Not found
            String notFound = "<html><body><h2>404 Not Found</h2><p>The requested file '" + fileName + "' was not found.</p></body></html>";
            String response = "HTTP/1.1 404 Not Found\r\n" +
                    "Content-Type: text/html; charset=UTF-8\r\n" +
                    "Content-Length: " + notFound.length() + "\r\n" +
                    "\r\n" +
                    notFound;
            toClient.write(response.getBytes("UTF-8"));
            toClient.flush();
            return;
        }
        // Serve file
        String contentType = getContentType(fileName);
        byte[] content = readFileBytes(file);
        String header = "HTTP/1.1 200 OK\r\n" +
                "Content-Type: " + contentType + "\r\n" +
                "Content-Length: " + content.length + "\r\n" +
                "\r\n";
        toClient.write(header.getBytes("UTF-8"));
        toClient.write(content);
        toClient.flush();
    }

    private String getContentType(String fileName) {
        if (fileName.endsWith(".html")) return "text/html";
        if (fileName.endsWith(".css")) return "text/css";
        if (fileName.endsWith(".js")) return "application/javascript";
        if (fileName.endsWith(".svg")) return "image/svg+xml";
        if (fileName.endsWith(".json")) return "application/json";
        return "application/octet-stream";
    }

    private byte[] readFileBytes(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int read;
            while ((read = fis.read(buffer)) != -1) {
                bos.write(buffer, 0, read);
            }
            return bos.toByteArray();
        }
    }

    @Override
    public void close() throws IOException {
        // Nothing to close
    }
}
