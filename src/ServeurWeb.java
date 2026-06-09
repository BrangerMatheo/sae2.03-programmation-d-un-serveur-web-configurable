
import java.io.*;
import java.util.*;

/**
 * Point d'entrée du serveur web configurable
 *
 *   java ServeurWeb [chemin/vers/conf.d]
 * Sans argument : cherche dans ~/serveurWeb/conf.d
 */
public class ServeurWeb {

    public static void main(String[] args) throws Exception {

        // Répertoire de configuration
        String confDir = args.length > 0
                ? args[0]
                : System.getProperty("user.home") + "/serveurWeb/conf.d";

        System.out.println("[ServeurWeb] Chargement de la config depuis : " + confDir);

        // Chargement des sites via ConfigLoader
        List<SiteConfig> sites = ConfigLoader.loadFromDirectory(confDir);

        if (sites.isEmpty()) {
            System.err.println("[ServeurWeb] Aucun site configuré. Arrêt.");
            System.exit(1);
        }

        // Fichier PID
        writePidFile();

        // Démarrage d'un SiteServer par site
        for (SiteConfig site : sites) {
            System.out.println("[ServeurWeb] Démarrage : " + site);
            new SiteServer(site).start();
        }

        System.out.println("[ServeurWeb] " + sites.size() + " site(s) démarré(s). Serveur prêt.");

        // Garde le thread principal en vie
        Thread.currentThread().join();
    }

    /**
     * Écrit le PID du processus Java dans ~/serveurWeb/run/myweb.pid
     */
    private static void writePidFile() {
        try {
            File runDir = new File(System.getProperty("user.home") + "/serveurWeb/run");
            runDir.mkdirs();

            File pidFile = new File(runDir, "myweb.pid");
            long pid = ProcessHandle.current().pid();

            try (FileWriter fw = new FileWriter(pidFile)) {
                fw.write(String.valueOf(pid));
            }

            System.out.println("[ServeurWeb] PID " + pid + " → " + pidFile.getAbsolutePath());
        } catch (Exception e) {
            System.err.println("[ServeurWeb] Impossible d'écrire le PID : " + e.getMessage());
        }
    }
}