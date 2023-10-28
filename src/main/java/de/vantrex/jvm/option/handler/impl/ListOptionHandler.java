package de.vantrex.jvm.option.handler.impl;

import de.vantrex.jvm.option.handler.OptionHandler;
import de.vantrex.jvm.service.JdkService;

import java.util.Arrays;

public class ListOptionHandler implements OptionHandler {

    private final JdkService jdkService = JdkService.INSTANCE;

    @Override
    public boolean handleOption(String[] args) {
        System.out.println("list option handler");
        System.out.println(Arrays.toString(args));
        if (args.length == 0 || !args[0].equalsIgnoreCase("remote")) {
            jdkService.displayLocalJdks();
            return true;
        }
        System.out.println("displaying remote jdks");
        jdkService.displayRemoteJdks();
        return true;
    }

    @Override
    public String[] getHelpString() {
        return new String[] {"Displays the current JDK´s available.",
                "Use \"%s list remote\" to display all the installable versions."};
    }

    @Override
    public String[] getOptionString() {
        return new String[] {"remote"};
    }
}