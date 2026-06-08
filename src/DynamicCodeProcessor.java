import java.io.*;
import java.util.regex.*;

/**
 * Traite les balises <code interpreteur="...">...</code> dans une page HTML.
 * Exécute le code via le processus spécifié et remplace la balise par le résultat.
 */
public class DynamicCodeProcessor {

    // Pattern qui capture l'attribut interpreteur et le contenu du code
    private static final Pattern CODE_TAG_PATTERN = Pattern.compile(
            "<code\\s+interpreteur=[\"«]([^\"»]+)[\"»]>(.*?)</code>",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE
    );

    /**
     * Traite le contenu HTML : remplace toutes les balises <code interpreteur=...>
     * par la sortie de l'exécution du code.
     */
    public static String process(String html) {
        StringBuffer result = new StringBuffer();
        Matcher matcher = CODE_TAG_PATTERN.matcher(html);

        while (matcher.find()) {
            String interpreter = matcher.group(1).trim();
            String code = matcher.group(2).trim();

            System.out.println("[DYNAMIC] Exécution via " + interpreter);
            String output = executeCode(interpreter, code);
            // Échapper les $ et \ pour replaceAll
            matcher.appendReplacement(result, Matcher.quoteReplacement(output.trim()));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    /**
     * Exécute le code avec l'interpréteur donné via un fichier temporaire.
     * Retourne la sortie standard ou un message d'erreur.
     */
    private static String executeCode(String interpreter, String code) {
        File tmpFile = null;
        try {
            // Extension selon l'interpréteur
            String ext = getExtension(interpreter);
            tmpFile = File.createTempFile("dyncode_", ext);
            tmpFile.deleteOnExit();

            // Écriture du code dans le fichier temporaire
            try (PrintWriter pw = new PrintWriter(new FileWriter(tmpFile))) {
                pw.print(code);
            }
            tmpFile.setExecutable(true);

            // Construction du processus
            ProcessBuilder pb = new ProcessBuilder(interpreter, tmpFile.getAbsolutePath());
            pb.redirectErrorStream(true); // stderr → stdout
            pb.environment().put("PATH", "/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin");

            Process proc = pb.start();

            // Lecture de la sortie
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(proc.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            // Attente avec timeout (5 secondes max)
            boolean finished = proc.waitFor(5, java.util.concurrent.TimeUnit.SECONDS);
            if (!finished) {
                proc.destroyForcibly();
                return "[Erreur : timeout dépassé]";
            }

            return output.toString();

        } catch (Exception e) {
            System.err.println("[DYNAMIC] Erreur exécution : " + e.getMessage());
            return "[Erreur : " + e.getMessage() + "]";
        } finally {
            if (tmpFile != null) tmpFile.delete();
        }
    }

    private static String getExtension(String interpreter) {
        if (interpreter.contains("python")) return ".py";
        if (interpreter.contains("bash") || interpreter.contains("sh")) return ".sh";
        if (interpreter.contains("perl")) return ".pl";
        if (interpreter.contains("ruby")) return ".rb";
        return ".tmp";
    }
}