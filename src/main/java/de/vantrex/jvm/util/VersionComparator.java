package de.vantrex.jvm.util;

import de.vantrex.jvm.jdk.Version;

import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VersionComparator implements Comparator<Version> {
    private static final Pattern pattern = Pattern.compile("\\D*(\\d+)");

    @Override
    public int compare(Version o1, Version o2) {
        final Matcher matcher = pattern.matcher(o1.version());
        final Matcher matcher2 = pattern.matcher(o2.version());
        int version1 = 0;
        int version2 = 0;
        if (matcher.find()) {
            version1 = Integer.parseInt(matcher.group(1));
        }
        if (matcher2.find()) {
            version2 = Integer.parseInt(matcher2.group(1));
        }
        return Integer.compare(version1, version2);
    }
}
