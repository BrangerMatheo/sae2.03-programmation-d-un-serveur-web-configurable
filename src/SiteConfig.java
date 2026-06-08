/**
 * Représente la configuration d'un site virtuel lu depuis le fichier XML.
 */
public class SiteConfig {
    private int port;
    private String documentRoot;
    private String defaultIndex;
    private String accessLog;
    private String errorLog;

    public SiteConfig() {}

    public int getPort() { return port; }
    public void setPort(int port) { this.port = port; }

    public String getDocumentRoot() { return documentRoot; }
    public void setDocumentRoot(String documentRoot) { this.documentRoot = documentRoot; }

    public String getDefaultIndex() { return defaultIndex != null ? defaultIndex : "index.html"; }
    public void setDefaultIndex(String defaultIndex) { this.defaultIndex = defaultIndex; }

    public String getAccessLog() { return accessLog; }
    public void setAccessLog(String accessLog) { this.accessLog = accessLog; }

    public String getErrorLog() { return errorLog; }
    public void setErrorLog(String errorLog) { this.errorLog = errorLog; }

    @Override
    public String toString() {
        return "SiteConfig{port=" + port + ", documentRoot='" + documentRoot + "', defaultIndex='" + getDefaultIndex() + "'}";
    }
}