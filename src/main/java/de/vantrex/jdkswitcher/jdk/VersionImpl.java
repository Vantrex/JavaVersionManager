package de.vantrex.jdkswitcher.jdk;

public class VersionImpl implements Version {

    private final String version;
    private final String jdkProvider;
    private final String operatingSystem;
    private final String systemArchitecture;
    private String downloadUrl;

    public VersionImpl(String version, String jdkProvider, String operatingSystem, String systemArchitecture, String downloadUrl) {
        this.version = version;
        this.jdkProvider = jdkProvider;
        this.operatingSystem = operatingSystem;
        this.systemArchitecture = systemArchitecture;
        this.downloadUrl = downloadUrl;
    }

    @Override
    public String version() {
        return this.version;
    }

    @Override
    public String jdkProvider() {
        return this.jdkProvider;
    }

    @Override
    public String operatingSystem() {
        return this.operatingSystem;
    }

    @Override
    public String systemArchitecture() {
        return this.systemArchitecture;
    }



    @Override
    public String downloadUrl() {
        return this.downloadUrl;
    }

    @Override
    public String compiledName() {
        return this.jdkProvider + "-" + version;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    @Override
    public String toString() {
        return jdkProvider + "-" + version + " for " + operatingSystem + " " + systemArchitecture;
    }
}