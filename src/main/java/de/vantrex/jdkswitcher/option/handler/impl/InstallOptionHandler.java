package de.vantrex.jdkswitcher.option.handler.impl;

import de.vantrex.jdkswitcher.jdk.Version;
import de.vantrex.jdkswitcher.option.handler.OptionHandler;
import de.vantrex.jdkswitcher.service.JDKService;

import java.util.Optional;

public class InstallOptionHandler implements OptionHandler {

    private final JDKService jdkService = JDKService.INSTANCE;

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
            System.out.println(version.toString() + " is already installed!");
            return true;
        }
        System.out.println("Installing " + version.toDirString() +  "..");
        if (!jdkService.isInstalled(version)) {
            jdkService.downloadJdk(version);
        }
        jdkService.switchToJdk(version);
        System.out.println("Installed " + version + "!");
        return false;
    }

    @Override
    public String[] getHelpString() {
        return new String[]{"The JDK version you want to install."};
    }

    @Override
    public String[] getOptionString() {
        return new String[]{"jdk"};
    }
}