package ru.sfedu.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Configuration utility. Allows to get configuration properties from the
 * default configuration file
 *
 * @author Kotsaba Eugeny
 */
public class ConfigurationUtil {

    private static final String DEFAULT_CONFIG_PATH = "environment.properties";
    private static String configPath = "";
    private static final Properties configuration = new Properties();

    public ConfigurationUtil() {

    }

    public static String getConfigPath() {
        return configPath;
    }

    public static void setConfigPath(String configPath) {
        ConfigurationUtil.configPath = configPath;
    }

    private static Properties getConfiguration() throws IOException {
        if (configuration.isEmpty()) {
            loadConfiguration();
        }
        return configuration;
    }

    /**
     * Loads configuration from <code>DEFAULT_CONFIG_PATH</code>
     *
     * @throws IOException In case of the configuration file read failure
     */
    private static void loadConfiguration() throws IOException {
        File nf;
        InputStream in;

        if (configPath == null || configPath.isEmpty()) {
            in = ConfigurationUtil.class.getClassLoader().getResourceAsStream(DEFAULT_CONFIG_PATH);
        } else {
            File file = new File(configPath);
            in = new FileInputStream(file);
        }

        try {
            configuration.load(in);
        } catch (IOException ex) {
            throw new IOException(ex);
        } finally {
            in.close();
        }
    }

    /**
     * Gets configuration entry value
     *
     * @param key Entry key
     * @return Entry value by key or nothing
     */
    public static String getConfigurationEntry(String key) {
        try {
            return getConfiguration().getProperty(key);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

}
