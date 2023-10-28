package de.vantrex.jvm.jdk.provider;

import de.vantrex.jvm.jdk.Version;

import java.util.Set;

public interface ProviderParser {

    void parse();

    Set<Version> getVersions();

}
