import java.util.*;

/**
 * Association extension de fichier → type MIME.
 */
public class MimeTypes {
    private static final Map<String, String> TYPES = new HashMap<>();

    static {
        // Texte
        TYPES.put("html", "text/html; charset=UTF-8");
        TYPES.put("htm",  "text/html; charset=UTF-8");
        TYPES.put("css",  "text/css");
        TYPES.put("js",   "application/javascript");
        TYPES.put("txt",  "text/plain; charset=UTF-8");
        TYPES.put("xml",  "application/xml");
        TYPES.put("json", "application/json");
        TYPES.put("csv",  "text/csv");

        // Images
        TYPES.put("png",  "image/png");
        TYPES.put("jpg",  "image/jpeg");
        TYPES.put("jpeg", "image/jpeg");
        TYPES.put("gif",  "image/gif");
        TYPES.put("ico",  "image/x-icon");
        TYPES.put("svg",  "image/svg+xml");
        TYPES.put("webp", "image/webp");

        // Documents
        TYPES.put("pdf",  "application/pdf");

        // Autres
        TYPES.put("zip",  "application/zip");
        TYPES.put("gz",   "application/gzip");
        TYPES.put("mp4",  "video/mp4");
        TYPES.put("mp3",  "audio/mpeg");
    }

    /** Retourne le type MIME pour l'extension donnée (sans point). */
    public static String get(String extension) {
        return TYPES.getOrDefault(extension.toLowerCase(), "application/octet-stream");
    }

    /** Retourne le type MIME depuis un nom de fichier complet. */
    public static String getFromFilename(String filename) {
        int dot = filename.lastIndexOf('.');
        if (dot >= 0 && dot < filename.length() - 1) {
            return get(filename.substring(dot + 1));
        }
        return "application/octet-stream";
    }

    /** Indique si ce type doit être compressé en gzip (images, pdf). */
    public static boolean shouldGzip(String filename) {
        int dot = filename.lastIndexOf('.');
        if (dot < 0) return false;
        String ext = filename.substring(dot + 1).toLowerCase();
        return ext.equals("png") || ext.equals("jpg") || ext.equals("jpeg")
                || ext.equals("gif") || ext.equals("pdf") || ext.equals("webp");
    }
}