package de.vantrex.jvm.option.handler.impl;

import de.vantrex.jvm.jdk.Version;
import de.vantrex.jvm.option.handler.OptionHandler;
import de.vantrex.jvm.service.JdkService;

import java.util.Optional;

public class CurrentOptionHandler implements OptionHandler {
    @Override
    public boolean handleOption(String[] args) {
        final Optional<Version> version = JdkService.INSTANCE.getCurrentVersion();
        if (!version.isPresent()) {
            System.out.println("There is currently no jdk version installed! Install one with jdk install [version].");
            return true;
        }
        version.ifPresent(v -> System.out.println("Currently installed: " + v));
        return true;
    }

    @Override
    public String[] getHelpString() {
        return new String[] {"Displays the current installed jdk version."};
    }

    @Override
    public String[] getOptionString() {
        return new String[0];
    }
}
