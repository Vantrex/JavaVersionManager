package de.vantrex.jdkswitcher.jdk.provider;

import de.vantrex.jdkswitcher.jdk.Version;
import de.vantrex.jdkswitcher.jdk.VersionImpl;
import de.vantrex.jdkswitcher.util.OSUtil;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;

public class AmazonCorrettoOpenJDK implements ProviderParser {

    private final JSONObject object;

    private final Set<Version> versions = new HashSet<>();

    public AmazonCorrettoOpenJDK(JSONObject object) {
        this.object = object;
        this.parse();
    }

    @Override
    public void parse() {
        // latest/amazon-corretto-[corretto_version]-[cpu_arch]-[os]-[package_type].[file_extension]
        try {
            final String os = OSUtil.getOperatingSystem().toLowerCase();
            final String arch = OSUtil.getArchitecture();
            final String baseUrl = object.getJSONObject("url-format").getString("base-url");
            final String fileFormat;
            if (os.equals("windows")) {
                fileFormat = "zip";
            } else {
                fileFormat = ".tar.gz";
            }
            for (Object o : object.getJSONArray("versions")) {
                final Integer versionNumber = (Integer) o;
                final String url = String.format(baseUrl,  versionNumber, arch, os, fileFormat, fileFormat);
                this.versions.add(new VersionImpl(String.valueOf(versionNumber), "Amazon-Corretto", os, arch, url));
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Set<Version> getVersions() {
        return this.versions;
    }
}
