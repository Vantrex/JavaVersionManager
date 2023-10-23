package de.vantrex.jdkswitcher.service;

import de.vantrex.jdkswitcher.jdk.Version;
import de.vantrex.jdkswitcher.util.Tuple;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;

import java.io.File;
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

    private final JdkService jdkService;
    private File installationDir;

    public DirectoryService(JdkService jdkService) {
        this.jdkService = jdkService;
        this.installationDir = new File(this.jdkService.getConfigurationProvider().getConfig().getInstallationDir(),
                "jdks");
        if (!installationDir.exists()) {
            if (!installationDir.mkdirs()) {
                throw new RuntimeException("File could not be created!");
            }
        }
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

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return to;
    }

    public File extractTarGzFile(String tarGzFilePath, String extractionPath) throws IOException {
        File to = null;

        try (TarArchiveInputStream tarInput = new TarArchiveInputStream(new GZIPInputStream(Files
                .newInputStream(Paths.get(tarGzFilePath))))) {
            TarArchiveEntry entry;
            byte[] buffer = new byte[1024];

            while ((entry = tarInput.getNextTarEntry()) != null) {
                String fileName = entry.getName();
                File destinationFile = new File(extractionPath, fileName);

                if (entry.isDirectory()) {
                    if (to == null)
                        to = destinationFile;
                    if (!destinationFile.mkdirs()) {
                        System.out.println("File could not be created, continuing anyways..");
                    }
                } else {
                    // Read the contents of the file into a byte array
                    FileOutputStream fos = new FileOutputStream(destinationFile);
                    int len;
                    while ((len = tarInput.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                    fos.close();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        File file = new File(extractionPath);
        System.out.println("file: " + file);
        return file;
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
