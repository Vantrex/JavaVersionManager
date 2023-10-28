package de.vantrex.jvm.config;

import de.vantrex.jvm.jdk.Version;
import de.vantrex.jvm.jdk.VersionImpl;
import de.vantrex.jvm.util.OSUtil;

@SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal", "CanBeFinal"})// IntelliJ is drunk, these fields are modified by Gson
public class JVMConfig {

    private VersionImpl currentVersion = null;
    private String installationDir = OSUtil.getPath();
    private boolean editPathVariables = true;
    private EnvConfig envConfig = new EnvConfig();

    public void setCurrentVersion(Version currentVersion) {
        this.currentVersion = (VersionImpl) currentVersion;
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
