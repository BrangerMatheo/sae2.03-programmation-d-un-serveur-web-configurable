import java.io.*;
import java.net.*;
import java.nio.file.*;

public class ServeurWeb {
    public static void main(String[] args) throws IOException {
        int port = 80; 
        
        String newDirectory = "";
        port = Integer.parseInt(args[0]);
        newDirectory = args[1];

        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Serveur HTTP démarré sur le port : " + port);

        while (true) {
            try (Socket client = serverSocket.accept();
                BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                OutputStream out = client.getOutputStream();
                PrintWriter headerOut = new PrintWriter(out, true)) {

                String requestLine = in.readLine();
                if (requestLine == null) continue;
                
                System.out.println("Requête reçue : " + requestLine);

                String[] tokens = requestLine.split(" ");
                if (tokens.length < 2) continue;
                
                String fileName = tokens[1];
                
                if (fileName.equals("/")) {
                    fileName = "/html/index.html";
                }   

                File file = new File(newDirectory + fileName);
                System.out.println(file);
                System.out.println(file);
                if (file.exists() && !file.isDirectory()) {
                    byte[] fileContent = Files.readAllBytes(file.toPath());
                    String contentType = getContentType(fileName);
                    
                    headerOut.print("HTTP/1.1 200 OK\r\n");
                    headerOut.print("Content-Type: " + contentType + "\r\n");
                    headerOut.print("Content-Length: " + fileContent.length + "\r\n");
                    headerOut.print("\r\n");
                    headerOut.flush();

                    out.write(fileContent);
                    out.flush();
                } else {
                    String errorMsg = "<h1>404 Not Found</h1>";
                    headerOut.print("HTTP/1.1 404 Not Found\r\n");
                    headerOut.print("Content-Type: text/html\r\n");
                    headerOut.print("Content-Length: " + errorMsg.length() + "\r\n");
                    headerOut.print("\r\n");
                    headerOut.print(errorMsg);
                    headerOut.flush();
                }
            } catch (Exception e) {
                System.err.println("Erreur lors du traitement : " + e.getMessage());
            }
        }
    }

    private static String getContentType(String fileName) {
        if (fileName.endsWith(".html") || fileName.endsWith(".htm")) return "text/html";
        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) return "image/jpeg";
        if (fileName.endsWith(".png")) return "image/png";
        if (fileName.endsWith(".css")) return "text/css";
        return "application/octet-stream";
    }
}