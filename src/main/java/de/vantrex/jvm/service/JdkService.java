package de.vantrex.jvm.service;


import de.vantrex.jvm.config.EnvConfig;
import de.vantrex.jvm.config.provider.ConfigurationProvider;
import de.vantrex.jvm.http.GistFetcher;
import de.vantrex.jvm.jdk.Version;
import de.vantrex.jvm.jdk.provider.AdoptOpenJDK;
import de.vantrex.jvm.jdk.provider.AmazonCorrettoOpenJDK;
import de.vantrex.jvm.jdk.provider.ProviderParser;
import de.vantrex.jvm.platform.IPlatform;
import de.vantrex.jvm.platform.impl.LinuxPlatform;
import de.vantrex.jvm.platform.impl.MacPlatform;
import de.vantrex.jvm.platform.impl.WindowsPlatform;
import de.vantrex.jvm.util.OSUtil;
import de.vantrex.jvm.util.Tuple;
import de.vantrex.jvm.util.VersionComparator;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class JdkService {

    public static final JdkService INSTANCE = new JdkService();

    private final Set<Version> versions = new HashSet<>();
    private final VersionComparator versionComparator = new VersionComparator();

    private JSONObject gist;
    private final ConfigurationProvider configurationProvider = new ConfigurationProvider();
    private final DirectoryService directoryService;
    private final IPlatform usedPlatform;

    public JdkService() {
        configurationProvider.load();
        try {
            new GistFetcher().fetchGist().ifPresent(jsonObject -> {
                gist = jsonObject;
                loadRemoteVersions();
            });
        } catch (IOException e) {
            System.out.println("AN ERROR OCCURRED, COULD NOT FETCH JDK LIST!");
            System.exit(0);
            throw new RuntimeException(e);
        }
        this.configurationProvider.save();
        this.directoryService = new DirectoryService(this);
        try {
            String operatingSystem = OSUtil.getOperatingSystem();
            switch (operatingSystem.toLowerCase()) {
                case "windows":
                    this.usedPlatform = new WindowsPlatform(this.configurationProvider, directoryService);
                    break;
                case "macos":
                    this.usedPlatform = new MacPlatform();
                    break;
                case "linux":
                    this.usedPlatform = new LinuxPlatform(configurationProvider, directoryService);
                    break;
                default:
                    this.usedPlatform = null;
                    System.out.println("Could not find platform! exiting..");
                    System.exit(0);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        this.init();
    }

    private void init() {
        this.usedPlatform.initPlatform();
        this.usedPlatform.findDefaultJdk();
    }

    public Optional<Version> getCurrentVersion() {
        return Optional.ofNullable(this.configurationProvider.getConfig().getCurrentVersion());
    }

    public boolean isDefaultInstallationAvailable() {
        final EnvConfig envConfig = this.configurationProvider.getConfig().getEnvConfig();
        if (envConfig.getPathToJavaBin() == null || envConfig.getDefaultJavaHome() == null) {
            return false;
        }
        File file = new File(envConfig.getPathToJavaBin());
        if (!(file.exists() && file.isDirectory())) {
            return false;
        }
        file = new File(envConfig.getDefaultJavaHome());
        return file.exists() && file.isDirectory();
    }

    public void switchToDefault() {
        usedPlatform.switchToDefault();
    }

    public void downloadJdk(Version version) {
        String path = directoryService.getInstallationDir() + File.separator + version.toDirString();
        this.downloadAndExtract(version.downloadUrl(), path);
    }

    public void installVersion(Version version) {
        this.usedPlatform.installVersion(version);
    }


    public boolean isDownloaded(Version version) {
        for (Tuple<Version, File> localJdkInstallation : this.directoryService.getLocalJdkInstallations()) {
            if (localJdkInstallation.getLeft().compiledName().equals(version.compiledName()))
                return true;
        }
        return false;
    }

    public Optional<Version> fromCompiledName(String name) {
        if (name.isEmpty())
            return Optional.empty();
        return this.versions.stream()
                .filter(Version::canUseOnOperatingSystem)
                .filter(version -> version.compiledName().equalsIgnoreCase(name)).findFirst();
    }

    private void loadRemoteVersions() {
        this.parseProvider(new AdoptOpenJDK(this.gist.getJSONObject("adopt")));
        this.parseProvider(new AmazonCorrettoOpenJDK(this.gist.getJSONObject("amazon-corretto")));
    }

    private void parseProvider(ProviderParser parser) {
        parser.parse();
        this.versions.addAll(parser.getVersions());
    }

    public void displayLocalJdks() {
        boolean displayed = false;
        for (Version version : this.directoryService.getLocalJdkInstallations()
                .stream()
                .map(Tuple::getLeft)
                .sorted(versionComparator).collect(Collectors.toList())) {
            if (version.canUseOnOperatingSystem()) {
                System.out.println(version);
                displayed = true;
            }
        }
        if (!displayed) {
            System.out.println("There are no local jdk installations!");
        }
    }

    public void displayRemoteJdks() {
        for (Version version : versions.stream()
                .sorted(versionComparator).collect(Collectors.toList())) {
            if (version.canUseOnOperatingSystem())
                System.out.println(version);
        }
    }

    public void downloadAndExtract(String fileUrl, String extractionPath) {
        final boolean isZip = fileUrl.endsWith(".zip");
        final String tempFileName = isZip ? "temp.zip" : "temp.tar.gz";
        try {
            URL url = new URL(fileUrl);
            try (InputStream in = url.openStream();
                 FileOutputStream out = new FileOutputStream(tempFileName)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }


            if (isZip) {
                this.directoryService.extractZipFile(tempFileName, extractionPath);
            } else {
                this.directoryService.extractTarGzFile(tempFileName, extractionPath);
            }
            File tempFile = new File(tempFileName);
            if (tempFile.exists()) {
                if (!tempFile.delete()) {
                    throw new RuntimeException("Could not delete temp file!");
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        final File file = new File(extractionPath);
        if (file.exists() && file.isDirectory() && Objects.requireNonNull(file.list()).length == 1) {
            final File to = Arrays.stream(Objects.requireNonNull(file.listFiles())).findFirst().orElse(null);
            if (to != null) {
                File tempFile = new File(UUID.randomUUID().toString());
                try {
                    FileUtils.copyDirectory(to, tempFile);
                    FileUtils.deleteDirectory(file);
                    FileUtils.copyDirectory(tempFile, new File(extractionPath));
                    FileUtils.deleteDirectory(tempFile);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        if (usedPlatform instanceof LinuxPlatform) {

            Set<PosixFilePermission> perms = new HashSet<>();
            perms.add(PosixFilePermission.OWNER_EXECUTE);
            perms.add(PosixFilePermission.OTHERS_EXECUTE);

            try {
                Files.setPosixFilePermissions(new File(extractionPath, "bin" + File.separator + "java").toPath(), perms);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public ConfigurationProvider getConfigurationProvider() {
        return configurationProvider;
    }

    public Set<Version> getVersions() {
        return versions;
    }
}
