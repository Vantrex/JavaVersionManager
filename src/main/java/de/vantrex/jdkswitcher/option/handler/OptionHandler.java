package de.vantrex.jdkswitcher.option.handler;

public interface OptionHandler {

    boolean handleOption(String[] args);

    String[] getHelpString();

    String[] getOptionString();

}