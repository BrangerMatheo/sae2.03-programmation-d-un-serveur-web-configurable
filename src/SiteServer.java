import java.io.*;
import java.net.*;

/**
 * Thread qui écoute sur le port d'un site et crée
 * un ClientHandler par connexion entrante.
 */
public class SiteServer extends Thread {

    private final SiteConfig config;
    private final Logger logger;

    public SiteServer(SiteConfig config) {
        this.config = config;
        this.logger = new Logger(config.getAccessLog(), config.getErrorLog());
        setName("SiteServer-port-" + config.getPort());
        setDaemon(false);
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(config.getPort())) {
            System.out.println("[SiteServer] Port " + config.getPort()
                    + " → " + config.getDocumentRoot());

            while (true) {
                Socket client = serverSocket.accept();
                Thread t = new Thread(new ClientHandler(client, config, logger));
                t.setDaemon(true);
                t.start();
            }
        } catch (IOException e) {
            System.err.println("[SiteServer] Erreur port " + config.getPort()
                    + " : " + e.getMessage());
        }
    }
}