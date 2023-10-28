package de.vantrex.jvm.config.provider;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import de.vantrex.jvm.config.JVMConfig;
import de.vantrex.jvm.util.OSUtil;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ConfigurationProvider {

    private static final Gson gson = new GsonBuilder()
            .serializeNulls()
            .setPrettyPrinting()
            .create();

    private final File configFolder = new File(OSUtil.getAppDataFolder(), "jvm");
    private final File file = new File(configFolder, "config.json");

    private JVMConfig config;

    public ConfigurationProvider() {
        if (!configFolder.exists()) {
            if (!configFolder.mkdirs()) {
                throw new RuntimeException("File could not be created!");
            }
        }
    }

    public void load() {
        if (!this.file.exists()) {
            try {
                if (!file.createNewFile()) {
                    throw new IOException("File could not be created!");
                }
                this.config = new JVMConfig();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return;
        }
        try (final FileReader fileReader = new FileReader(this.file)) {
            this.config = gson.fromJson(fileReader, TypeToken.get(JVMConfig.class));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void save() {
        try (final FileWriter fileWriter = new FileWriter(this.file)) {
            gson.toJson(config, fileWriter);
            fileWriter.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public File getConfigFolder() {
        return configFolder;
    }

    public JVMConfig getConfig() {
        return config;
    }
}