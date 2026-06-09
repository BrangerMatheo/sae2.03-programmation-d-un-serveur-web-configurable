import java.io.*;
import java.util.zip.*;

/**
 * Compresse des données en gzip.
 * La décision de compresser ou non est déléguée à MimeTypes.shouldGzip().
 */
public class GzipEncoder {

    /** Compresse un tableau de bytes en gzip. */
    public static byte[] compress(byte[] data) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (GZIPOutputStream gzip = new GZIPOutputStream(bos)) {
            gzip.write(data);
        }
        return bos.toByteArray();
    }
}