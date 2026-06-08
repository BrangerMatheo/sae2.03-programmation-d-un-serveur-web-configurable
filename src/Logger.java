import java.io.*;
import java.time.*;
import java.time.format.*;

/**
 * Logger simple pour les accès et les erreurs.
 * Thread-safe via synchronisation.
 */
public class Logger {
    private final String accessLogPath;
    private final String errorLogPath;
    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("dd/MMM/yyyy:HH:mm:ss Z");

    public Logger(String accessLogPath, String errorLogPath) {
        this.accessLogPath = accessLogPath;
        this.errorLogPath = errorLogPath;
        // Créer les répertoires si nécessaire
        ensureDir(accessLogPath);
        ensureDir(errorLogPath);
    }

    private void ensureDir(String path) {
        if (path == null) return;
        File f = new File(path);
        if (f.getParentFile() != null) {
            f.getParentFile().mkdirs();
        }
    }

    /** Log une requête HTTP au format Combined Log Format. */
    public synchronized void logAccess(String clientIp, String method, String uri, int status, long bytes) {
        String line = clientIp + " - - [" + ZonedDateTime.now().format(FMT) + "] \""
                + method + " " + uri + " HTTP/1.1\" " + status + " " + bytes;
        writeLine(accessLogPath, line);
        System.out.println("[ACCESS] " + line);
    }

    /** Log une erreur. */
    public synchronized void logError(String message) {
        String line = "[" + ZonedDateTime.now().format(FMT) + "] [ERROR] " + message;
        writeLine(errorLogPath, line);
        System.err.println("[ERROR] " + message);
    }

    private void writeLine(String path, String line) {
        if (path == null) return;
        try (FileWriter fw = new FileWriter(path, true);
             PrintWriter pw = new PrintWriter(fw)) {
            pw.println(line);
        } catch (IOException e) {
            System.err.println("[LOGGER] Impossible d'écrire dans " + path + " : " + e.getMessage());
        }
    }
}