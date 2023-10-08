package de.vantrex.jdkswitcher.jdk.parser;

import de.vantrex.jdkswitcher.jdk.Version;
import de.vantrex.jdkswitcher.jdk.VersionImpl;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class AdoptOpenJDK implements ProviderParser {

    private final JSONObject object;

    private final Set<Version> versions = new HashSet<>();

    private final List<String> possibleOperationSystems = Arrays.asList("windows", "linux", "macOS");
    private final List<String> possibleArchitectures = Arrays.asList("x64", "x86", "aarch64", "arm");
    private final Pattern pattern = Pattern.compile("openjdk-\\d+");

    public AdoptOpenJDK(JSONObject object) {
        this.object = object;
    }


    @Override
    public void parse() {
        for (String versions : object.keySet()) {
            if (pattern.matcher(versions).find()) {
                this.parseVersion(versions);
            }
        }
    }

    private void parseVersion(String version) {
        for (String operatingSystem : object.getJSONObject(version).keySet()) {
            if (this.possibleOperationSystems.contains(operatingSystem)) {
                this.parseArchitecture(version, operatingSystem);
            }
        }
    }

    private void parseArchitecture(String version, String operatingSystem) {
        for (String possibleArchitecture : object.getJSONObject(version).getJSONObject(operatingSystem).keySet()) {
            if (this.possibleArchitectures.contains(possibleArchitecture)) {
                this.versions.add(new VersionImpl(
                        version,
                        "Adopt",
                        operatingSystem,
                        possibleArchitecture,
                        object.getJSONObject(version).getJSONObject(operatingSystem).getString(possibleArchitecture))
                );
            }
        }
    }

    @Override
    public Set<Version> getVersions() {
        return this.versions;
    }


}
