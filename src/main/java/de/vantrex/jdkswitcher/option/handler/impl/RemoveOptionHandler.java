package de.vantrex.jdkswitcher.option.handler.impl;

import de.vantrex.jdkswitcher.option.handler.OptionHandler;

public class RemoveOptionHandler implements OptionHandler {
    @Override
    public boolean handleOption(String[] args) {
        return false;
    }

    @Override
    public String[] getHelpString() {
        return new String[] {"Removes a jdk"};
    }

    @Override
    public String[] getOptionString() {
        return new String[]{"jdk"};
    }
}
