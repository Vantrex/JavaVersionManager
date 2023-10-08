package de.vantrex.jdkswitcher.util;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class OSUtil {

    public static String getOperatingSystem() throws Exception {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            return "Windows";
        } else if (os.contains("mac")) {
            return "macOS";
        } else if (os.contains("nix") || os.contains("nux")) {
            // Check for Alpine Linux
            String osVersion = System.getProperty("os.version").toLowerCase();
            if (osVersion.contains("alpine")) {
                throw new Exception("Unsupported Linux distribution: Alpine Linux");
            }
            return "Linux";
        } else {
            throw new Exception("Unknown Operating System");
        }
    }


    public static String getArchitecture() {
        String arch = System.getProperty("os.arch").toLowerCase();
        if (arch.contains("64") || arch.contains("amd64") || arch.contains("x86_64")) {
            // Format arm64 into x64 for Windows
            if (System.getProperty("os.name").toLowerCase().contains("win") && arch.contains("amd64")) {
                return "x64";
            }
            return arch;
        } else if (arch.contains("arm")) {
            return "arm";
        } else {
            return "x86";
        }
    }

    public static String getAppDataFolder() {
        String os = System.getProperty("os.name").toLowerCase();
        String userHome = System.getProperty("user.home");

        if (os.contains("win")) {
            // Windows
            String appData = System.getenv("APPDATA");
            if (appData != null) {
                return appData;
            } else {
                return userHome + "\\AppData\\Roaming";
            }
        } else if (os.contains("mac")) {
            // macOS
            return userHome + "/Library/Application Support";
        } else if (os.contains("nix") || os.contains("nux")) {
            // Linux
            return userHome + "/.config";
        } else {
            // Other OS, handle accordingly
            return getPath();
        }
    }

    public static String getPath() {
        String jarFilePath = OSUtil.class.getProtectionDomain().getCodeSource().getLocation().getPath();

        try {
            jarFilePath = java.net.URLDecoder.decode(jarFilePath, "UTF-8");
        } catch (UnsupportedEncodingException e) {
           throw new RuntimeException(e);
        }
        return new File(jarFilePath).getParent();
    }

}