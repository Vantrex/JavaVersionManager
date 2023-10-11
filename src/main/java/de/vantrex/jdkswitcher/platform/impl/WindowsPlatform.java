package de.vantrex.jdkswitcher.platform.impl;

import de.vantrex.jdkswitcher.config.EnvConfig;
import de.vantrex.jdkswitcher.config.JDKSwitcherConfig;
import de.vantrex.jdkswitcher.config.provider.ConfigurationProvider;
import de.vantrex.jdkswitcher.jdk.Version;
import de.vantrex.jdkswitcher.jna.NativeHook;
import de.vantrex.jdkswitcher.platform.IPlatform;
import de.vantrex.jdkswitcher.service.DirectoryService;
import de.vantrex.jdkswitcher.service.JdkService;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Arrays;

public class WindowsPlatform implements IPlatform {
    private final ConfigurationProvider configurationProvider;
    private final DirectoryService directoryService;

    public WindowsPlatform(ConfigurationProvider configurationProvider, DirectoryService directoryService) {
        this.configurationProvider = configurationProvider;
        this.directoryService = directoryService;
    }

    @Override
    public void initPlatform() {
        System.setProperty("jna.library.path", this.configurationProvider.getConfigFolder().getAbsolutePath());
        this.copyNativeDll();
    }

    @Override
    public void installVersion(Version version) {
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

    @Override
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

    @Override
    public void findDefaultJdk() {
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

    private void copyNativeDll() {

        String resourceName = "SystemEnvLib.dll";
        final File targetFile = new File(this.configurationProvider.getConfigFolder(), resourceName);
        if (targetFile.exists()) {
            if (targetFile.lastModified() > System.currentTimeMillis() - 86400000L)
                return;
            if (!targetFile.delete())
                return;
        }
        try (InputStream inputStream = JdkService.class.getResourceAsStream("/" + resourceName);
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

}
