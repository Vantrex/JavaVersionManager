package de.vantrex.jvm.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CommandRunner {

    public static List<String> handleBashCommand(final String command, final String[] args) throws IOException {
        final List<String> commandList = new ArrayList<>(Collections.singletonList(command));
        commandList.addAll(Arrays.asList(args));
        final ProcessBuilder processBuilder = new ProcessBuilder(commandList);
        final Process process = processBuilder.start();
        final List<String> output = new ArrayList<>();
        try (final InputStream inputStream = process.getInputStream();
             final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String str;
            while ((str = reader.readLine()) != null) {
                output.add(str);
            }
        }
        return output;
    }

}
