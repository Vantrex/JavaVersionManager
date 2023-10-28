package de.vantrex.jvm.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class JavaPathMatcher {

    private static final Pattern JDK_PATH_PATTERN = Pattern.compile("^(.*/)?(jdk|openjdk|java-)(.*/)?bin$");

    private JavaPathMatcher() {
    }

    public static boolean isJdkPath(final String path) {
        final Matcher matcher = JDK_PATH_PATTERN.matcher(path);
        final boolean matches = matcher.matches();
        System.out.println(path + " " + matches);
        return matches;
    }
}