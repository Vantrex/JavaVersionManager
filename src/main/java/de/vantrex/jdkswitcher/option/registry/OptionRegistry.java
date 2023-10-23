package de.vantrex.jdkswitcher.option.registry;

import de.vantrex.jdkswitcher.option.RegisteredOption;
import de.vantrex.jdkswitcher.option.handler.impl.*;

import java.util.HashSet;
import java.util.Set;

public class OptionRegistry {

    private final Set<RegisteredOption> options = new HashSet<>();

    public OptionRegistry() {
        this.registerDefaults();
    }

    private void registerDefaults() {
        this.options.add(new RegisteredOption("current", new String[]{"-c", "-current"}, 0, 0, new CurrentOptionHandler()));
        this.options.add(new RegisteredOption("install", new String[]{"i", "-i", "-install"}, 1, 1, new InstallOptionHandler()));
        this.options.add(new RegisteredOption("remove", new String[]{"rm", "-rm", "-remove"}, 1, 1, new RemoveOptionHandler()));
        this.options.add(new RegisteredOption("list", new String[]{"-l"}, 0, 1, new ListOptionHandler()));
        this.options.add(new RegisteredOption("default", new String[]{"-d", "-default"}, 0, 0, new DefaultOptionHandler()));
    }

    public Set<RegisteredOption> getOptions() {
        return options;
    }
}