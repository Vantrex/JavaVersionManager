package de.vantrex.jdkswitcher.service;


import de.vantrex.jdkswitcher.config.EnvConfig;
import de.vantrex.jdkswitcher.config.JDKSwitcherConfig;
import de.vantrex.jdkswitcher.config.provider.ConfigurationProvider;
import de.vantrex.jdkswitcher.http.GistFetcher;
import de.vantrex.jdkswitcher.jdk.Version;
import de.vantrex.jdkswitcher.jdk.parser.AdoptOpenJDK;
import de.vantrex.jdkswitcher.jdk.parser.AmazonCorrettoOpenJDK;
import de.vantrex.jdkswitcher.jna.NativeHook;
import de.vantrex.jdkswitcher.util.Tuple;
import de.vantrex.jdkswitcher.util.VersionComparator;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class JDKService {

    public static final JDKService INSTANCE = new JDKService();

    private final Set<Version> versions = new HashSet<>();
    private final VersionComparator versionComparator = new VersionComparator();

    private JSONObject gist;
    private final ConfigurationProvider configurationProvider = new ConfigurationProvider();
    private final DirectoryService directoryService;

    public JDKService() {
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
        this.copyNativeDll();
        this.init();
    }

    private void copyNativeDll() {
        String jarPath = JDKService.class.getProtectionDomain().getCodeSource().getLocation().getPath();

        String resourceName = "SystemEnvLib.dll";
        final File targetFile = new File(this.configurationProvider.getConfigFolder(), resourceName);
        if (targetFile.exists()) {
            this.init();
            if (targetFile.lastModified() > System.currentTimeMillis() - 86400000L)
                return;
            if (!targetFile.delete())
                return;
        }
        System.out.println("copying");
        try (InputStream inputStream = JDKService.class.getResourceAsStream("/" + resourceName);
             OutputStream outputStream = Files.newOutputStream(targetFile.toPath())) {

            byte[] buffer = new byte[1024];
            int bytesRead;
            //noinspection DataFlowIssue
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void init() {
        System.setProperty("jna.library.path", this.configurationProvider.getConfigFolder().getAbsolutePath());
        EnvConfig envConfig = this.configurationProvider.getConfig().getEnvConfig();
        boolean changedFile = false;
        if (envConfig.getDefaultJavaHome() == null) {
            envConfig.setDefaultJavaHome(NativeHook.INSTANCE.getEnvVariable("JAVA_HOME"));
            changedFile = true;
        }
        if (envConfig.getPathToJavaBin() == null) {
            for (String path : NativeHook.INSTANCE.getEnvVariable("PATH").split(";")) {
                if ((path.toLowerCase().contains("java") || path.toLowerCase().contains("jdk"))
                        && path.endsWith("bin")) {
                    System.out.println("Possible jdk path detected, saving path to file!");
                    envConfig.setPathToJavaBin(path);
                    changedFile = true;
                    break;
                }
            }
        }
        if (changedFile) {
            this.configurationProvider.save();
        }
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
        final EnvConfig envConfig = this.configurationProvider.getConfig().getEnvConfig();
        String pathVariable = findNativeJavaPath();
        String[] pathArray = NativeHook.INSTANCE.getEnvVariable("PATH").split(";");
        if (pathVariable == null) {
            pathArray = Arrays.copyOf(pathArray, pathArray.length + 1);
            pathArray[pathArray.length - 1] = envConfig.getPathToJavaBin();
        } else {
            for (int i = 0; i < pathArray.length; i++) {
                if (pathArray[i].equals(pathVariable)) {
                    pathArray[i] = envConfig.getPathToJavaBin();
                }
            }
        }
        NativeHook.INSTANCE.setEnvVariable("JAVA_HOME", envConfig.getDefaultJavaHome());
        if (this.configurationProvider.getConfig().isEditPathVariables()) {
            NativeHook.INSTANCE.setEnvVariable("PATH", String.join(";", pathArray));
        }
        NativeHook.INSTANCE.restartExplorer();
    }

    public void downloadJdk(Version version) {
        JDKSwitcherConfig config = this.configurationProvider.getConfig();
        String path = directoryService.getInstallationDir() + File.separator + version.toDirString();
        this.downloadAndExtract(version.downloadUrl(), path);
    }

    public void switchToJdk(Version version) {
        JDKSwitcherConfig config = this.configurationProvider.getConfig();
        String path = this.directoryService.getInstallationDir() + File.separator + version.toDirString();
        String binPath = path + File.separator + "bin";
        if (binPath.contains(";"))
            throw new UnsupportedOperationException("Path cannot contain \";\"!");
        config.setCurrentVersion(version);
        NativeHook.INSTANCE.setEnvVariable("JAVA_HOME", path);
        if (this.configurationProvider.getConfig().isEditPathVariables())
            NativeHook.INSTANCE.setEnvVariable("PATH", String.join(";", switchJavaPath(binPath)));
        configurationProvider.save();
        NativeHook.INSTANCE.restartExplorer();
    }

    private String[] switchJavaPath(String newPath) {
        String[] pathArray = NativeHook.INSTANCE.getEnvVariable("PATH").split(";");
        String pathVariable = findNativeJavaPath();
        for (int i = 0; i < pathArray.length; i++) {
            if (pathArray[i].equals(pathVariable)) {
                pathArray[i] = newPath;
            }
        }
        return pathArray;
    }

    private String findNativeJavaPath() {
        for (String path : NativeHook.INSTANCE.getEnvVariable("PATH").split(";")) {
            if ((path.toLowerCase().contains("java") || path.toLowerCase().contains("jdk"))
                    && path.endsWith("bin")) {
                return path;
            }
        }
        return null;
    }

    public boolean isInstalled(Version version) {
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
        AdoptOpenJDK adoptOpenJDK = new AdoptOpenJDK(this.gist.getJSONObject("adopt"));
        adoptOpenJDK.parse();
        this.versions.addAll(adoptOpenJDK.getVersions());
        AmazonCorrettoOpenJDK correttoOpenJDK = new AmazonCorrettoOpenJDK(this.gist.getJSONObject("amazon-corretto"));
        this.versions.addAll(correttoOpenJDK.getVersions());
    }

    public void displayLocalJdks() {
        Pattern pattern = Pattern.compile("\\D*(\\d+)");

        for (Version version : this.directoryService.getLocalJdkInstallations()
                .stream()
                .map(Tuple::getLeft)
                .sorted(versionComparator).collect(Collectors.toList())) {
            if (version.canUseOnOperatingSystem())
                System.out.println(version);
        }

    }

    public void displayRemoteJdks() {
        Pattern pattern = Pattern.compile("\\D*(\\d+)");
        for (Version version : versions.stream()
                .sorted(versionComparator).collect(Collectors.toList())) {
            if (version.canUseOnOperatingSystem())
                System.out.println(version);
        }
    }

    public void downloadAndExtract(String fileUrl, String extractionPath) {
        final boolean isZip = fileUrl.endsWith(".zip");
        File to = null;
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
                to = this.directoryService.extractZipFile(tempFileName, extractionPath);
            } else {
                to = this.directoryService.extractTarGzFile(tempFileName, extractionPath);
            }
            File tempFile = new File(tempFileName);
            if (tempFile.exists()) {
                tempFile.delete();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        final File file = new File(extractionPath);
        if (file.exists() && file.isDirectory() && Objects.requireNonNull(file.list()).length == 1) {
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
    }

    public ConfigurationProvider getConfigurationProvider() {
        return configurationProvider;
    }

    public Set<Version> getVersions() {
        return versions;
    }
}
