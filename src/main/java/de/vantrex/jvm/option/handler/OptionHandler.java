package de.vantrex.jvm.option.handler;

public interface OptionHandler {

    boolean handleOption(String[] args);

    String[] getHelpString();

    String[] getOptionString();

}