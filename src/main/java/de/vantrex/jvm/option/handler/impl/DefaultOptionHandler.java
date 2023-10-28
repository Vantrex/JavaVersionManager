package de.vantrex.jvm.option.handler.impl;

import de.vantrex.jvm.option.handler.OptionHandler;
import de.vantrex.jvm.service.JdkService;

public class DefaultOptionHandler implements OptionHandler {
    @Override
    public boolean handleOption(String[] args) {
        JdkService jdkService = JdkService.INSTANCE;
        if (!jdkService.getCurrentVersion().isPresent()) {
            System.out.println("There is no java version currently installed!");
        }
        System.out.println("Trying to switch to default installation..");
        if (!jdkService.isDefaultInstallationAvailable()) {
            System.out.println("There is no default installation available!");
            return true;
        }
        System.out.println("Found default installation! Switching to it..");
        jdkService.switchToDefault();
        System.out.println("Switched to the default jdk!");
        jdkService.getConfigurationProvider().getConfig().setCurrentVersion(null);
        jdkService.getConfigurationProvider().save();
        return true;
    }

    @Override
    public String[] getHelpString() {
        return new String[] {"Installs the default jdk version if one is set"};
    }

    @Override
    public String[] getOptionString() {
        return new String[0];
    }
}
