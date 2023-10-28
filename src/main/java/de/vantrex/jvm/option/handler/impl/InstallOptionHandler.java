package de.vantrex.jvm.option.handler.impl;

import de.vantrex.jvm.jdk.Version;
import de.vantrex.jvm.option.handler.OptionHandler;
import de.vantrex.jvm.service.JdkService;

import java.util.Optional;

public class InstallOptionHandler implements OptionHandler {

    private final JdkService jdkService = JdkService.INSTANCE;

    @Override
    public boolean handleOption(String[] args) {
        final String toInstall = args[0];
        final Optional<Version> versionOptional = jdkService.fromCompiledName(toInstall);
        if (!versionOptional.isPresent()) {
            System.out.println("Version not found!");
            return true;
        }
        final Version version = versionOptional.get();
        if (jdkService.getCurrentVersion().filter(version1 -> version.toString().equals(version1.toString())).isPresent()) {
            System.out.println(version + " is already installed!");
            return true;
        }
        System.out.println("Installing " + version.toDirString() +  "..");
        if (!jdkService.isDownloaded(version)) {
            jdkService.downloadJdk(version);
        }
        jdkService.installVersion(version);
        System.out.println("Installed " + version + "!");
        return true;
    }

    @Override
    public String[] getHelpString() {
        return new String[]{"The JDK version you want to install."};
    }

    @Override
    public String[] getOptionString() {
        return new String[]{"jdk-version"};
    }
}