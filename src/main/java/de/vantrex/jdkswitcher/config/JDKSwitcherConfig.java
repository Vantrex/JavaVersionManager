package de.vantrex.jdkswitcher.config;

import de.vantrex.jdkswitcher.jdk.Version;
import de.vantrex.jdkswitcher.jdk.VersionImpl;

public class JDKSwitcherConfig {

    private VersionImpl currentVersion = null;
    private String installationDir = System.getProperty("user.dir");
    private boolean editPathVariables = true;
    private EnvConfig envConfig = new EnvConfig();

    public void setCurrentVersion(Version currentVersion) {
        this.currentVersion = (VersionImpl) currentVersion;
    }

    public void setInstallationDir(String installationDir) {
        this.installationDir = installationDir;
    }

    public String getInstallationDir() {
        return installationDir;
    }

    public Version getCurrentVersion() {
        return currentVersion;
    }

    public EnvConfig getEnvConfig() {
        return envConfig;
    }

    public boolean isEditPathVariables() {
        return editPathVariables;
    }
}
