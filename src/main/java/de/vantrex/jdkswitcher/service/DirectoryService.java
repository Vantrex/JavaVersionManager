package de.vantrex.jdkswitcher.service;

import de.vantrex.jdkswitcher.jdk.Version;
import de.vantrex.jdkswitcher.util.Tuple;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class DirectoryService {

    private final JDKService jdkService;
    private File installationDir;

    public DirectoryService(JDKService jdkService) {
        this.jdkService = jdkService;
        this.installationDir = new File(this.jdkService.getConfigurationProvider().getConfig().getInstallationDir(),
                "jdks");
        if (!installationDir.exists())
            installationDir.mkdirs();
    }

    public Set<Tuple<Version, File>> getLocalJdkInstallations() {
        if (!installationDir.exists())
            return new HashSet<>();
        final Set<Tuple<Version, File>> installations = new HashSet<>();
        for (File file : Objects.requireNonNull(installationDir.listFiles(File::isDirectory))) {
            for (Version version : this.jdkService.getVersions()) {
                if (file.getName().equals(version.toDirString())) {
                    installations.add(new Tuple<>(version, file));
                }
            }
        }
        return installations;
    }

    public File getInstallationDir() {
        return installationDir;
    }

    public void setInstallationDir(File installationDir) {
        this.installationDir = installationDir;
    }

    public File extractZipFile(String zipFilePath, String extractionPath) {
        File to = null;
        final File destDir = new File(extractionPath);
        byte[] buffer = new byte[1024];
        try (ZipInputStream zipInputStream = new ZipInputStream(Files.newInputStream(Paths.get(zipFilePath)))) {
            ZipEntry zipEntry = zipInputStream.getNextEntry();

            while (zipEntry != null) {
                File newFile = newFile(destDir, zipEntry);
                if (zipEntry.isDirectory()) {
                    if (to == null)
                        to = newFile;
                    if (!newFile.isDirectory() && !newFile.mkdirs()) {
                        throw new IOException("Failed to create directory " + newFile);
                    }
                } else {
                    // fix for Windows-created archives
                    File parent = newFile.getParentFile();
                    if (!parent.isDirectory() && !parent.mkdirs()) {
                        throw new IOException("Failed to create directory " + parent);
                    }

                    // write file content
                    FileOutputStream fos = new FileOutputStream(newFile);
                    int len;
                    while ((len = zipInputStream.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                    fos.close();
                }
                zipEntry = zipInputStream.getNextEntry();
            }

            System.out.println("ZIP-Datei erfolgreich entpackt.");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return to;
    }

    public File extractTarGzFile(String tarGzFilePath, String extractionPath) {
        File to = null;
        try (TarArchiveInputStream tarInput = new TarArchiveInputStream(new GZIPInputStream(new FileInputStream(tarGzFilePath)))) {
            TarArchiveEntry entry;
            while ((entry = tarInput.getNextTarEntry()) != null) {
                String filePath = extractionPath + File.separator + entry.getName();

                // Erstelle die Verzeichnisstruktur, falls notwendig
                if (entry.isDirectory()) {
                    if (to == null) {
                        to = new File(filePath);
                    }
                    new File(filePath).mkdirs();
                } else {
                    byte[] content = new byte[(int) entry.getSize()];
                    tarInput.read(content);
                    try (FileOutputStream fos = new FileOutputStream(filePath)) {
                        fos.write(content);
                    }
                }
            }

            System.out.println("TAR.GZ-Datei erfolgreich entpackt.");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return to;
    }

    private File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }

}
