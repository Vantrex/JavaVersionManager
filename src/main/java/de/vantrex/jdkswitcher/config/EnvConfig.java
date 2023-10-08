package de.vantrex.jdkswitcher.config;

public class EnvConfig {

    private String defaultJavaHome = null;
    private String pathToJavaBin = null;

    public String getDefaultJavaHome() {
        return defaultJavaHome;
    }

    public void setDefaultJavaHome(String defaultJavaHome) {
        this.defaultJavaHome = defaultJavaHome;
    }

    public String getPathToJavaBin() {
        return pathToJavaBin;
    }

    public void setPathToJavaBin(String pathToJavaBin) {
        this.pathToJavaBin = pathToJavaBin;
    }

}