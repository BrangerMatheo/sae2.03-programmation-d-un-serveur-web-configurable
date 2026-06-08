import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.*;
import java.util.*;

/**
 * Charge et parse les fichiers de configuration XML (.conf) depuis un répertoire.
 * Supporte plusieurs fichiers (un par site) et un fichier global.
 */
public class ConfigLoader {

    /**
     * Charge tous les fichiers .conf du répertoire donné.
     * Retourne la liste de tous les SiteConfig trouvés.
     */
    public static List<SiteConfig> loadFromDirectory(String confDir) throws Exception {
        List<SiteConfig> sites = new ArrayList<>();
        File dir = new File(confDir);

        if (!dir.exists() || !dir.isDirectory()) {
            throw new FileNotFoundException("Répertoire de configuration introuvable : " + confDir);
        }

        File[] confFiles = dir.listFiles((d, name) -> name.endsWith(".conf"));
        if (confFiles == null || confFiles.length == 0) {
            throw new FileNotFoundException("Aucun fichier .conf trouvé dans : " + confDir);
        }

        Arrays.sort(confFiles); // ordre déterministe
        for (File f : confFiles) {
            System.out.println("[CONFIG] Lecture : " + f.getAbsolutePath());
            sites.addAll(parseFile(f));
        }

        return sites;
    }

    /**
     * Parse un fichier de configuration XML.
     * Supporte à la fois <webconf><site>...</site></webconf>
     * et un fichier contenant directement <site>...</site>.
     */
    private static List<SiteConfig> parseFile(File file) throws Exception {
        List<SiteConfig> sites = new ArrayList<>();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(file);
        doc.getDocumentElement().normalize();

        NodeList siteNodes = doc.getElementsByTagName("site");
        for (int i = 0; i < siteNodes.getLength(); i++) {
            Node siteNode = siteNodes.item(i);
            if (siteNode.getNodeType() == Node.ELEMENT_NODE) {
                SiteConfig config = parseSiteElement((Element) siteNode);
                if (config != null) {
                    sites.add(config);
                    System.out.println("[CONFIG] Site chargé : " + config);
                }
            }
        }
        return sites;
    }

    private static SiteConfig parseSiteElement(Element siteElem) {
        SiteConfig config = new SiteConfig();

        String portStr = getTextContent(siteElem, "port");
        if (portStr == null || portStr.isEmpty()) {
            System.err.println("[CONFIG] Erreur : élément <site> sans <port>, ignoré.");
            return null;
        }
        try {
            config.setPort(Integer.parseInt(portStr.trim()));
        } catch (NumberFormatException e) {
            System.err.println("[CONFIG] Port invalide : " + portStr);
            return null;
        }

        config.setDocumentRoot(getTextContent(siteElem, "DocumentRoot"));
        config.setDefaultIndex(getTextContent(siteElem, "DefaultIndex"));
        config.setAccessLog(getTextContent(siteElem, "Acceslog"));
        config.setErrorLog(getTextContent(siteElem, "Errorlog"));

        return config;
    }

    private static String getTextContent(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() > 0) {
            return nodes.item(0).getTextContent().trim();
        }
        return null;
    }
}