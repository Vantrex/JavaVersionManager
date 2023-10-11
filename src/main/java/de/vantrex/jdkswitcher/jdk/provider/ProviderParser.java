package de.vantrex.jdkswitcher.jdk.provider;

import de.vantrex.jdkswitcher.jdk.Version;

import java.util.Set;

public interface ProviderParser {

    void parse();

    Set<Version> getVersions();

}
