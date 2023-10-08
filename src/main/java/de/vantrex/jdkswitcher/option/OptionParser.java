package de.vantrex.jdkswitcher.option;

import de.vantrex.jdkswitcher.option.handler.OptionHandler;
import de.vantrex.jdkswitcher.option.registry.OptionRegistry;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Collectors;

public class OptionParser {

    private final String[] args;

    private final OptionRegistry registry = new OptionRegistry();


    public OptionParser(final String[] args) {
        this.args = args;
        this.handleArguments();
    }

    private void handleArguments() {
        final Optional<OptionHandler> optionHandler = this.searchHandler();
        if (!optionHandler.isPresent()) {
            displayHelpPage();
            System.exit(0);
            return;
        }
        final String[] optionArguments = new String[args.length - 1];
        System.arraycopy(args, 1, optionArguments, 0, optionArguments.length);
        optionHandler.ifPresent(optionHandler1 -> optionHandler1.handleOption(optionArguments));
    }

    private void displayHelpPage() {
        StringBuilder sb = new StringBuilder();
        final long maxOptionStringLength = this.registry.getOptions().stream()
                .map(registeredOption -> registeredOption.getName()
                        + Arrays.stream(registeredOption.getHandler().getOptionString()).map(s -> s = " [" + s + "]")
                        .collect(Collectors.joining("")))
                .max(Comparator.comparingInt(String::length)).orElse("").length();
        for (RegisteredOption option : this.registry.getOptions()
                .stream()
                .sorted(Comparator.comparingInt(o -> o.getName().length()))
                .collect(Collectors.toList())) {
            StringBuilder optionStr = new StringBuilder("jvm " + option.getName()
                    + Arrays.stream(option.getHandler().getOptionString())
                    .map(s -> s = " [" + s + "]")
                    .collect(Collectors.joining("")));
            if (optionStr.toString().length() < maxOptionStringLength) {
                for (int i = optionStr.length(); i < maxOptionStringLength; i++) {
                    optionStr.append("\t");
                }
                optionStr.append(" ");
            }
            optionStr.append(" : ");
            System.out.print(optionStr);
            int length = optionStr.toString().length();
            int c = 0;
            for (String helpStr : option.getHandler().getHelpString()) {
                helpStr = String.format(helpStr, "jvm");
                if (++c == 1) {
                    System.out.print(helpStr);
                } else {
                    String helpSb = " " + helpStr;
                    System.out.println(helpSb);
                }
            }
            if (option.getHandler().getHelpString().length == 1)
                System.out.println();
        }
    }

    private Optional<OptionHandler> searchHandler() {
        if (args.length == 0) {
            return Optional.empty();
        }
        final String name = args[0];
        return this.registry.getOptions()
                .stream()
                .filter(registeredOption ->
                        registeredOption.getName().equalsIgnoreCase(name)
                                || Arrays.stream(registeredOption.getAliases()).anyMatch(s -> s.equalsIgnoreCase(name)))
                .map(RegisteredOption::getHandler)
                .findFirst();
    }
}