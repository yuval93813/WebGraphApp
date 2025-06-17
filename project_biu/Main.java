import server.HTTPServer;
import server.MyHTTPServer;
import servlets.*;

public class Main {
    public static void main(String[] args) throws Exception {
        HTTPServer server = new MyHTTPServer(8080, 5);

        // server.addServlet("GET", "/publish", new TopicDisplayer());
        server.addServlet("POST", "/upload", new ConfLoader());
        // Use relative path from project_biu directory to html_files
        server.addServlet("GET", "/app/", new HtmlLoader("../html_files"));

        server.start();
        System.out.println("Server started on port 8080");
        System.out.println("Access the application at: http://localhost:8080/app/index.html");
        System.out.println("Press Enter to stop the server...");
        System.in.read();  // Wait for user input before shutting down
        server.close();
        System.out.println("Server stopped.");
    }
}
