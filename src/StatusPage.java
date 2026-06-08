import java.io.*;
import java.nio.file.*;

/**
 * Génère la page d'état de la machine accessible via /status.
 * Lit les informations système depuis /proc.
 */
public class StatusPage {

    /**
     * Génère le HTML complet de la page /status.
     */
    public static String generate() {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n<html>\n<head>\n");
        html.append("<meta charset=\"UTF-8\">\n");
        html.append("<title>État du serveur</title>\n");
        html.append("<style>\n");
        html.append("body { font-family: Arial, sans-serif; margin: 40px; background: #f5f5f5; }\n");
        html.append("h1 { color: #333; border-bottom: 2px solid #666; }\n");
        html.append("table { border-collapse: collapse; width: 60%; background: white; }\n");
        html.append("td, th { border: 1px solid #ddd; padding: 10px 16px; }\n");
        html.append("th { background: #4a7c59; color: white; text-align: left; }\n");
        html.append("tr:nth-child(even) { background: #f9f9f9; }\n");
        html.append(".ok { color: green; font-weight: bold; }\n");
        html.append(".warn { color: orange; font-weight: bold; }\n");
        html.append("</style>\n</head>\n<body>\n");
        html.append("<h1>&#128202; État du Serveur Web</h1>\n");

        html.append("<table>\n");
        html.append("<tr><th>Paramètre</th><th>Valeur</th></tr>\n");

        // Mémoire disponible
        html.append("<tr><td>Mémoire disponible</td><td>").append(getMemoryAvailable()).append("</td></tr>\n");

        // Mémoire totale
        html.append("<tr><td>Mémoire totale</td><td>").append(getMemoryTotal()).append("</td></tr>\n");

        // Espace disque disponible
        html.append("<tr><td>Espace disque disponible</td><td>").append(getDiskAvailable()).append("</td></tr>\n");

        // Espace disque total
        html.append("<tr><td>Espace disque total</td><td>").append(getDiskTotal()).append("</td></tr>\n");

        // Nombre de processus
        html.append("<tr><td>Nombre de processus</td><td>").append(getProcessCount()).append("</td></tr>\n");

        // Charge CPU (load average)
        html.append("<tr><td>Charge CPU (1/5/15 min)</td><td>").append(getLoadAverage()).append("</td></tr>\n");

        // Uptime
        html.append("<tr><td>Temps de fonctionnement</td><td>").append(getUptime()).append("</td></tr>\n");

        // JVM
        html.append("<tr><td>JVM</td><td>").append(System.getProperty("java.version")).append("</td></tr>\n");

        // Date
        html.append("<tr><td>Date serveur</td><td>").append(new java.util.Date()).append("</td></tr>\n");

        html.append("</table>\n");
        html.append("<p><a href=\"/\">← Retour à l'accueil</a></p>\n");
        html.append("</body>\n</html>\n");

        return html.toString();
    }

    /** Lit la mémoire disponible depuis /proc/meminfo. */
    private static String getMemoryAvailable() {
        return readProcMeminfo("MemAvailable");
    }

    private static String getMemoryTotal() {
        return readProcMeminfo("MemTotal");
    }

    private static String readProcMeminfo(String key) {
        try {
            for (String line : Files.readAllLines(Paths.get("/proc/meminfo"))) {
                if (line.startsWith(key + ":")) {
                    String[] parts = line.trim().split("\\s+");
                    long kb = Long.parseLong(parts[1]);
                    return formatSize(kb * 1024);
                }
            }
        } catch (Exception e) {
            return "Indisponible";
        }
        return "Indisponible";
    }

    /** Retourne l'espace disque disponible sur le filesystem racine. */
    private static String getDiskAvailable() {
        try {
            File root = new File("/");
            return formatSize(root.getFreeSpace());
        } catch (Exception e) {
            return "Indisponible";
        }
    }

    private static String getDiskTotal() {
        try {
            File root = new File("/");
            return formatSize(root.getTotalSpace());
        } catch (Exception e) {
            return "Indisponible";
        }
    }

    /** Compte les processus via /proc. */
    private static String getProcessCount() {
        try {
            File proc = new File("/proc");
            int count = 0;
            File[] entries = proc.listFiles();
            if (entries != null) {
                for (File f : entries) {
                    if (f.isDirectory()) {
                        try {
                            Integer.parseInt(f.getName());
                            count++;
                        } catch (NumberFormatException ignored) {}
                    }
                }
            }
            return String.valueOf(count);
        } catch (Exception e) {
            return "Indisponible";
        }
    }

    /** Lit le load average depuis /proc/loadavg. */
    private static String getLoadAverage() {
        try {
            String content = new String(Files.readAllBytes(Paths.get("/proc/loadavg")));
            String[] parts = content.trim().split("\\s+");
            return parts[0] + " / " + parts[1] + " / " + parts[2];
        } catch (Exception e) {
            return "Indisponible";
        }
    }

    /** Lit l'uptime depuis /proc/uptime. */
    private static String getUptime() {
        try {
            String content = new String(Files.readAllBytes(Paths.get("/proc/uptime")));
            double totalSeconds = Double.parseDouble(content.trim().split("\\s+")[0]);
            long h = (long)(totalSeconds / 3600);
            long m = (long)((totalSeconds % 3600) / 60);
            long s = (long)(totalSeconds % 60);
            return String.format("%dh %02dm %02ds", h, m, s);
        } catch (Exception e) {
            return "Indisponible";
        }
    }

    /** Formate les octets en unité lisible. */
    private static String formatSize(long bytes) {
        if (bytes >= 1024L * 1024 * 1024) {
            return String.format("%.2f Go", bytes / (1024.0 * 1024 * 1024));
        } else if (bytes >= 1024 * 1024) {
            return String.format("%.2f Mo", bytes / (1024.0 * 1024));
        } else if (bytes >= 1024) {
            return String.format("%.2f Ko", bytes / 1024.0);
        }
        return bytes + " o";
    }
}