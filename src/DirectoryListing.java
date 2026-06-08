import java.io.*;
import java.text.*;
import java.util.*;

/**
 * Génère une page HTML de listing de répertoire avec des liens navigables.
 */
public class DirectoryListing {

    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    /**
     * Génère la page HTML listant le contenu du répertoire.
     * @param dir Le répertoire à lister
     * @param uriPath Le chemin URI demandé (pour les liens)
     */
    public static String generate(File dir, String uriPath) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n<html>\n<head>\n");
        html.append("<meta charset=\"UTF-8\">\n");
        html.append("<title>Index de ").append(escapeHtml(uriPath)).append("</title>\n");
        html.append("<style>\n");
        html.append("body { font-family: monospace; margin: 30px; }\n");
        html.append("h1 { font-size: 1.2em; }\n");
        html.append("table { border-collapse: collapse; width: 80%; }\n");
        html.append("td, th { padding: 5px 12px; text-align: left; }\n");
        html.append("th { border-bottom: 2px solid #333; }\n");
        html.append("tr:hover { background: #f0f0f0; }\n");
        html.append(".dir { font-weight: bold; color: #0055cc; }\n");
        html.append(".file { color: #333; }\n");
        html.append(".size { color: #666; text-align: right; }\n");
        html.append("</style>\n</head>\n<body>\n");
        html.append("<h1>Index de ").append(escapeHtml(uriPath)).append("</h1>\n");
        html.append("<hr>\n");
        html.append("<table>\n");
        html.append("<tr><th>Nom</th><th>Taille</th><th>Dernière modification</th></tr>\n");

        // Lien vers le répertoire parent
        if (!uriPath.equals("/")) {
            String parent = uriPath.endsWith("/") ? uriPath.substring(0, uriPath.length() - 1) : uriPath;
            int lastSlash = parent.lastIndexOf('/');
            String parentUri = lastSlash <= 0 ? "/" : parent.substring(0, lastSlash) + "/";
            html.append("<tr><td class=\"dir\"><a href=\"").append(parentUri).append("\">..</a></td><td></td><td></td></tr>\n");
        }

        File[] files = dir.listFiles();
        if (files != null) {
            Arrays.sort(files, (a, b) -> {
                // Répertoires en premier, puis tri alphabétique
                if (a.isDirectory() && !b.isDirectory()) return -1;
                if (!a.isDirectory() && b.isDirectory()) return 1;
                return a.getName().compareToIgnoreCase(b.getName());
            });

            for (File f : files) {
                String name = f.getName();
                String href = uriPath.endsWith("/") ? uriPath + name : uriPath + "/" + name;
                if (f.isDirectory()) href += "/";

                html.append("<tr>");
                html.append("<td class=\"").append(f.isDirectory() ? "dir" : "file").append("\">");
                html.append("<a href=\"").append(escapeHtml(href)).append("\">").append(escapeHtml(name));
                if (f.isDirectory()) html.append("/");
                html.append("</a></td>");

                html.append("<td class=\"size\">");
                if (f.isFile()) html.append(formatSize(f.length()));
                html.append("</td>");

                html.append("<td>").append(DATE_FMT.format(new Date(f.lastModified()))).append("</td>");
                html.append("</tr>\n");
            }
        }

        html.append("</table>\n<hr>\n");
        html.append("<small>ServeurWeb SAE 2.03</small>\n");
        html.append("</body>\n</html>\n");
        return html.toString();
    }

    private static String escapeHtml(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    private static String formatSize(long bytes) {
        if (bytes >= 1024 * 1024) return String.format("%.1f Mo", bytes / (1024.0 * 1024));
        if (bytes >= 1024) return String.format("%.1f Ko", bytes / 1024.0);
        return bytes + " o";
    }
}