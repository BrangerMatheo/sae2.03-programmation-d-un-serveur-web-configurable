import java.io.*;
import java.nio.file.*;
import java.security.*;
import java.time.*;
import java.time.format.*;
import java.util.Locale;

/**
 * Gère le cache HTTP :
 *  - ETag via MD5 du fichier
 *  - Last-Modified
 *  - Détection 304 Not Modified (If-None-Match / If-Modified-Since)
 */
public class CacheManager {

    private static final DateTimeFormatter HTTP_DATE =
            DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.ENGLISH)
                    .withZone(ZoneOffset.UTC);

    /** Calcule le ETag MD5 d'un fichier. */
    public static String computeETag(File file) throws Exception {
        byte[] content = Files.readAllBytes(file.toPath());
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] digest = md.digest(content);
        StringBuilder sb = new StringBuilder("\"");
        for (byte b : digest) sb.append(String.format("%02x", b));
        sb.append("\"");
        return sb.toString();
    }

    /** Retourne la date de dernière modification au format HTTP. */
    public static String getLastModified(File file) {
        return HTTP_DATE.format(Instant.ofEpochMilli(file.lastModified()));
    }

    /** Retourne la date courante au format HTTP (header Date:). */
    public static String now() {
        return HTTP_DATE.format(Instant.now());
    }

    /**
     * Retourne true si la ressource n'a PAS changé → répondre 304.
     */
    public static boolean isNotModified(File file, String ifNoneMatch, String ifModifiedSince) {
        // Priorité au ETag
        if (ifNoneMatch != null && !ifNoneMatch.isEmpty()) {
            try {
                return computeETag(file).equals(ifNoneMatch.trim());
            } catch (Exception e) {
                return false;
            }
        }
        // Sinon date
        if (ifModifiedSince != null && !ifModifiedSince.isEmpty()) {
            try {
                Instant clientDate = Instant.from(HTTP_DATE.parse(ifModifiedSince.trim()));
                Instant fileDate   = Instant.ofEpochMilli(file.lastModified())
                        .truncatedTo(java.time.temporal.ChronoUnit.SECONDS);
                return !fileDate.isAfter(clientDate);
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }
}