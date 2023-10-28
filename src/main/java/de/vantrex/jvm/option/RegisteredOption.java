package de.vantrex.jvm.option;

import de.vantrex.jvm.option.handler.OptionHandler;

public class RegisteredOption {

    private final String name;
    private final String[] aliases;

    private final int minLength;
    private final int maxLength;
    private final OptionHandler handler;

    public RegisteredOption(String name, String[] aliases, int minLength, int maxLength, OptionHandler handler) {
        this.name = name;
        this.aliases = aliases;
        this.minLength = minLength;
        this.maxLength = maxLength;
        this.handler = handler;
    }

    public String getName() {
        return name;
    }

    public String[] getAliases() {
        return aliases;
    }

    public int getMinLength() {
        return minLength;
    }

    public int getMaxLength() {
        return maxLength;
    }

    public OptionHandler getHandler() {
        return handler;
    }
}
