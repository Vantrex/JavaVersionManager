package de.vantrex.jvm.platform.impl;

import de.vantrex.jvm.config.EnvConfig;
import de.vantrex.jvm.config.JVMConfig;
import de.vantrex.jvm.config.provider.ConfigurationProvider;
import de.vantrex.jvm.jdk.Version;
import de.vantrex.jvm.platform.IPlatform;
import de.vantrex.jvm.service.DirectoryService;
import de.vantrex.jvm.util.CommandRunner;
import de.vantrex.jvm.util.JavaPathMatcher;
import de.vantrex.jvm.util.ProfileLine;
import org.apache.commons.compress.utils.Lists;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class LinuxPlatform implements IPlatform {

    private final ConfigurationProvider configurationProvider;
    private final DirectoryService directoryService;

    // helper variables, if java home is not exported yet.
    private String javaHomeEnv = "";
    private String pathEnv = "";

    public LinuxPlatform(ConfigurationProvider configurationProvider, DirectoryService directoryService) {
        this.configurationProvider = configurationProvider;
        this.directoryService = directoryService;
    }

    @Override
    public void initPlatform() {

    }

    @Override
    public void installVersion(Version version) {
        try {
            final List<ProfileLine> lines = this.getCurrentDotBashrcContent();
            final List<ProfileLine> toEdit = this.findLinesToEditInDotBashrcFile(lines);
            System.out.println(lines.stream().map(ProfileLine::getContent).collect(Collectors.toList()));
            System.out.println("^lines");
            System.out.println(toEdit);
            System.out.println("^toEdit");
            final String javaHomeEnv = "JAVA_HOME=" + directoryService.getInstallationDir() + File.separator + version.toDirString();
            replaceDotProfileAndSubmit(lines, toEdit, javaHomeEnv);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void switchToDefault() {
        final JVMConfig config = this.configurationProvider.getConfig();
        try {
            final List<ProfileLine> lines = this.getCurrentDotBashrcContent();
            final List<ProfileLine> toEdit = this.findLinesToEditInDotBashrcFile(lines);
            final String javaHomeEnv = "JAVA_HOME=" + config.getEnvConfig().getDefaultJavaHome();
            replaceDotProfileAndSubmit(lines, toEdit, javaHomeEnv);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void replaceDotProfileAndSubmit(List<ProfileLine> lines, List<ProfileLine> toEdit, String javaHomeEnv) throws IOException {
        for (ProfileLine line : lines) {
            if (line.getContent().startsWith("export ")) // we do not have to edit exports, they should be in the correct order tho. TODO: edit all of them?
                continue;
            if (toEdit.contains(line) && line.getContent().startsWith("JAVA_HOME=")) {
                line.setContent(javaHomeEnv);
            }
        }
        writeToBashrc(lines);
//        CommandRunner.handleBashCommand("bash", new String[]{"-c", "export", javaHomeEnv});
 //       CommandRunner.handleBashCommand("bash", new String[]{"-c", "export", "PATH=" + "\"$JAVA_HOME" + File.separator + "bin:$PATH\""});
        for (String bash : CommandRunner.handleBashCommand("bash", new String[]{"-c", "java", "-version"})) {
            System.out.println(bash);
        }
    }


    @Override
    public void findDefaultJdk() {
        final EnvConfig envConfig = this.configurationProvider.getConfig().getEnvConfig();
        boolean changedFile = false;
        try {
            List<String> output = CommandRunner.handleBashCommand("which", new String[]{"java"});
            System.out.println(output);
            if (output.isEmpty()) {
                throw new RuntimeException("No java installation found!");
            }
            final File javaDir = findJavaDir(output.get(0));
            if (!hasJavaHome()) {
                System.out.println("found no default java home!");
                exportDefaultJavaHome(javaDir.getParentFile().getAbsolutePath());
            }

            if (envConfig.getDefaultJavaHome() == null) {
                envConfig.setDefaultJavaHome(System.getenv().getOrDefault("JAVA_HOME", this.javaHomeEnv.isEmpty() ? null : this.javaHomeEnv));
                changedFile = true;
            }
            if (envConfig.getPathToJavaBin() == null) {
                boolean contained = false;
                for (String path : System.getenv().getOrDefault("PATH", this.pathEnv + ":").split(":")) {
                    if ((path.toLowerCase().contains("java") || path.toLowerCase().contains("jdk"))
                            && path.endsWith("bin")) {
                        System.out.println("Possible jdk path detected, saving path to file!");
                        envConfig.setPathToJavaBin(path);
                        changedFile = true;
                        contained = true;
                        break;
                    }
                }
                if (!contained && pathEnv != null) {
                    envConfig.setPathToJavaBin(pathEnv);
                    changedFile = true;
                }
            }


        } catch (IOException ignored) {

        }
        if (changedFile) {
            this.configurationProvider.save();
        }
    }

    private File findJavaDir(String searchPath) throws IOException {
        List<String> output = CommandRunner.handleBashCommand("readlink", new String[]{searchPath});
        if (output.isEmpty()) {
            return new File(searchPath);
            //throw new RuntimeException("No java installation found!");
        }
        if (!isJavaFolder(output.get(0))) {
            return findJavaDir(output.get(0));
        }
        return new File(output.get(0)).getParentFile();
    }

    private boolean isJavaFolder(String path) {

        final File file = new File(path);
        if (!file.exists())
            throw new RuntimeException(new FileNotFoundException("File does not exist."));
        final File parentFile = file.getParentFile();
        return JavaPathMatcher.isJdkPath(parentFile.getAbsolutePath()) && parentFile.isDirectory();
    }

    private boolean hasJavaHome() {
        final String javaHome = System.getenv("JAVA_HOME");
        return javaHome != null && !javaHome.isEmpty();
    }

    private void exportDefaultJavaHome(final String javaHome) throws IOException {
        if (this.configurationProvider.getConfig().getEnvConfig().getPathToJavaBin() != null &&
                this.configurationProvider.getConfig().getEnvConfig().getDefaultJavaHome() != null) {
            return;
        }
        final List<ProfileLine> profileLines = this.getCurrentDotBashrcContent();
        int currentIndex = profileLines.stream().map(ProfileLine::getIndex).max(Integer::compare).orElse(0);
        if (currentIndex != 0 && !profileLines.get(profileLines.size() - 1).getContent().isEmpty()) {
            profileLines.add(new ProfileLine(++currentIndex, "\n"));
            currentIndex++;
        }
        profileLines.add(new ProfileLine(currentIndex, "# Added by Java Version Manager"));
        profileLines.add(new ProfileLine(++currentIndex, String.format("JAVA_HOME='%s'", javaHome)));
        profileLines.add(new ProfileLine(++currentIndex, "export JAVA_HOME"));
        profileLines.add(new ProfileLine(++currentIndex, "PATH=\"$JAVA_HOME" + File.separator + "bin:$PATH\""));
        profileLines.add(new ProfileLine(++currentIndex, "export PATH"));
        this.writeToBashrc(profileLines);
        CommandRunner.handleBashCommand("bash", new String[]{"-c", "export", "JAVA_HOME=" + javaHome});
        this.pathEnv = javaHome + File.separator + "bin";
        this.javaHomeEnv = javaHome;
        CommandRunner.handleBashCommand("bash", new String[]{"-c", "export", "PATH=" + "\"$JAVA_HOME" + File.separator + "bin:$PATH\""});
    }

    private void writeToBashrc(final List<ProfileLine> profileLines) {
        final File profileFiles = new File(System.getProperty("user.home"), ".bashrc");
        try (final FileWriter fileWriter = new FileWriter(profileFiles)) {
            for (ProfileLine profileLine : profileLines) {
                final StringBuilder toWrite = new StringBuilder();
                toWrite.append(profileLine.getContent());
                if (profileLine.getIndex() != profileLines.size() - 1) {
                    toWrite.append("\n");
                }
                fileWriter.write(toWrite.toString());
            }
            fileWriter.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<ProfileLine> getCurrentDotBashrcContent() throws IOException {
        final File profileFiles = new File(System.getProperty("user.home"), ".bashrc");
        if (!profileFiles.exists()) {
            // TODO SHOULD WE CREATE ONE?
            return Lists.newArrayList();
        }
        final List<ProfileLine> profileLines = new ArrayList<>();
        try (final FileReader reader = new FileReader(profileFiles);
             final BufferedReader bufferedReader = new BufferedReader(reader)) {
            String str;
            int index = 0;
            while ((str = bufferedReader.readLine()) != null) {
                profileLines.add(new ProfileLine(index, str));
                index++;
            }
        }
        return profileLines;
    }

    private List<ProfileLine> findLinesToEditInDotBashrcFile(final List<ProfileLine> profileLines) {
        final List<ProfileLine> toEdit = new ArrayList<>();
        final int totalLines = profileLines.size();
        lineLoop:
        for (int index = 0; index < profileLines.size(); index++) {
            if (totalLines - 1 == index) { /* we can skip the last line.
                                        We have to have at least 2 (optimal 3) consecutive lines. */
                break;
            }
            final ProfileLine lineAtIndex = profileLines.get(index);
            final String contentAtIndex = lineAtIndex.getContent();
            if (contentAtIndex.startsWith("#")) {
                continue;
            }
            if (contentAtIndex.startsWith("JAVA_HOME=")) {
                for (int j = index + 1; j < index + 5; j++) {
                    if (profileLines.size() == j) {
                        break;
                    }
                    final ProfileLine lineBeneathIndex = profileLines.get(j);
                    final String contentBeneath = lineBeneathIndex.getContent();
                    if (contentBeneath.startsWith("PATH=\"$JAVA_HOME")) {
                        lineBeneathIndex.setLineType(ProfileLine.LineType.BIN_PATH);
                    } else if (contentBeneath.equals("export PATH") || contentBeneath.equals("export JAVA_HOME")) {
                        lineBeneathIndex.setLineType(ProfileLine.LineType.EXPORT);
                    } else {
                        continue lineLoop;
                    }
                    toEdit.add(lineBeneathIndex);
                }
                toEdit.add(lineAtIndex);
            }
        }
        return toEdit;
    }
}