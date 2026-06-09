import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;

/**
 * Traite une connexion HTTP dans un thread séparé.
 * Utilise : MimeTypes, DirectoryListing, DynamicCodeProcessor,
 *           StatusPage, CacheManager, GzipEncoder, Logger.
 */
public class ClientHandler implements Runnable {

    private final Socket socket;
    private final SiteConfig config;
    private final Logger logger;

    public ClientHandler(Socket socket, SiteConfig config, Logger logger) {
        this.socket = socket;
        this.config = config;
        this.logger = logger;
    }

    @Override
    public void run() {
        String clientIp = socket.getInetAddress().getHostAddress();
        String method = "-";
        String uri    = "-";

        try (
                BufferedReader in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                OutputStream   out = socket.getOutputStream()
        ) {
            // Lecture de la ligne de requête
            String requestLine = in.readLine();
            if (requestLine == null || requestLine.isEmpty()) return;

            String[] tokens = requestLine.split(" ");
            if (tokens.length < 2) return;
            method = tokens[0];
            uri    = tokens[1];

            // Lecture des en-têtes
            Map<String, String> headers = new HashMap<>();
            String line;
            while ((line = in.readLine()) != null && !line.isEmpty()) {
                int colon = line.indexOf(':');
                if (colon > 0) {
                    headers.put(line.substring(0, colon).trim().toLowerCase(),
                            line.substring(colon + 1).trim());
                }
            }

            // Décodage et nettoyage du chemin
            String path = uri.contains("?") ? uri.substring(0, uri.indexOf('?')) : uri;
            path = URLDecoder.decode(path, "UTF-8");

            // Sécurité : interdit la traversée de répertoires
            if (path.contains("..")) {
                sendError(out, 403, "Forbidden", method, uri, clientIp);
                return;
            }

            // Route /status
            if (path.equals("/status")) {
                String html = StatusPage.generate();
                sendHtml(out, 200, "OK", html, method, uri, clientIp);
                return;
            }

            // Résolution du fichier
            File file = resolveFile(path);

            if (file == null || !file.exists()) {
                sendError(out, 404, "Not Found", method, uri, clientIp);
                return;
            }

            // Répertoire
            if (file.isDirectory()) {
                handleDirectory(out, file, path, method, uri, clientIp);
                return;
            }

            // Cache (ETag / If-Modified-Since)
            String ifNoneMatch   = headers.get("if-none-match");
            String ifModSince    = headers.get("if-modified-since");
            if (CacheManager.isNotModified(file, ifNoneMatch, ifModSince)) {
                sendNotModified(out);
                logger.logAccess(clientIp, method, uri, 304, 0);
                return;
            }

            // Lecture du contenu
            byte[] content = Files.readAllBytes(file.toPath());
            String contentType = MimeTypes.getFromFilename(file.getName());

            // Traitement dynamique pour HTML
            if (contentType.startsWith("text/html")) {
                String html = DynamicCodeProcessor.process(new String(content, "UTF-8"));
                content = html.getBytes("UTF-8");
            }

            // Gzip pour images / PDF
            String acceptEncoding = headers.getOrDefault("accept-encoding", "");
            boolean useGzip = MimeTypes.shouldGzip(file.getName()) && acceptEncoding.contains("gzip");
            byte[] body = useGzip ? GzipEncoder.compress(content) : content;

            // Envoi de la réponse
            String etag    = CacheManager.computeETag(file);
            String lastMod = CacheManager.getLastModified(file);

            PrintWriter pw = new PrintWriter(new OutputStreamWriter(out), false);
            pw.print("HTTP/1.1 200 OK\r\n");
            pw.print("Date: "           + CacheManager.now()  + "\r\n");
            pw.print("Server: MyWebServer/1.0\r\n");
            pw.print("Content-Type: "   + contentType         + "\r\n");
            pw.print("Content-Length: " + body.length         + "\r\n");
            pw.print("ETag: "           + etag                + "\r\n");
            pw.print("Last-Modified: "  + lastMod             + "\r\n");
            pw.print("Cache-Control: max-age=3600\r\n");
            if (useGzip) pw.print("Content-Encoding: gzip\r\n");
            pw.print("\r\n");
            pw.flush();
            out.write(body);
            out.flush();

            logger.logAccess(clientIp, method, uri, 200, body.length);

        } catch (Exception e) {
            logger.logError("Erreur client " + clientIp + " [" + method + " " + uri + "] : " + e.getMessage());
        } finally {
            try { socket.close(); } catch (IOException ignored) {}
        }
    }

    // Résolution chemin → fichier

    private File resolveFile(String path) {
        String root = config.getDocumentRoot();
        if (path.equals("/")) {
            // Essaie le DefaultIndex, sinon retourne le répertoire racine
            File index = new File(root, config.getDefaultIndex());
            return index.exists() ? index : new File(root);
        }
        return new File(root, path);
    }

    // Répertoire : DefaultIndex ou listing

    private void handleDirectory(OutputStream out, File dir, String uriPath,
                                 String method, String uri, String clientIp) throws Exception {
        // Cherche le DefaultIndex dans ce répertoire
        File index = new File(dir, config.getDefaultIndex());
        if (index.exists()) {
            byte[] content = Files.readAllBytes(index.toPath());
            String html = DynamicCodeProcessor.process(new String(content, "UTF-8"));
            sendHtml(out, 200, "OK", html, method, uri, clientIp);
            return;
        }
        String html = DirectoryListing.generate(dir, uriPath);
        sendHtml(out, 200, "OK", html, method, uri, clientIp);
    }

    // Helpers d'envoi

    private void sendHtml(OutputStream out, int code, String status, String html,
                          String method, String uri, String clientIp) throws Exception {
        byte[] body = html.getBytes("UTF-8");
        PrintWriter pw = new PrintWriter(new OutputStreamWriter(out), false);
        pw.print("HTTP/1.1 " + code + " " + status + "\r\n");
        pw.print("Date: " + CacheManager.now() + "\r\n");
        pw.print("Server: MyWebServer/1.0\r\n");
        pw.print("Content-Type: text/html; charset=UTF-8\r\n");
        pw.print("Content-Length: " + body.length + "\r\n");
        pw.print("\r\n");
        pw.flush();
        out.write(body);
        out.flush();
        logger.logAccess(clientIp, method, uri, code, body.length);
    }

    private void sendError(OutputStream out, int code, String status,
                           String method, String uri, String clientIp) throws Exception {
        String html = "<!DOCTYPE html><html><body><h1>" + code + " " + status + "</h1></body></html>";
        sendHtml(out, code, status, html, method, uri, clientIp);
        logger.logError(code + " " + status + " pour " + clientIp + " [" + method + " " + uri + "]");
    }

    private void sendNotModified(OutputStream out) throws Exception {
        PrintWriter pw = new PrintWriter(new OutputStreamWriter(out), false);
        pw.print("HTTP/1.1 304 Not Modified\r\n");
        pw.print("Date: " + CacheManager.now() + "\r\n");
        pw.print("Server: MyWebServer/1.0\r\n");
        pw.print("\r\n");
        pw.flush();
    }
}