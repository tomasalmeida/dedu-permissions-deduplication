package com.tomasalmeida.dedu.api.system;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Auxiliar class to read properties files
 */
public class PropertiesLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesLoader.class);

    /**
     * Load properties from a given file
     */
    public static Properties loadFromFile(final String filePath) throws IOException {
        if (!Files.exists(Paths.get(filePath))) {
            LOGGER.error("File [{}] was not found. Stopping...", filePath);
            throw new IOException(filePath + " not found.");
        }
        final Properties properties = new Properties();
        try (final InputStream inputStream = new FileInputStream(filePath)) {
            properties.load(inputStream);
        }
        LOGGER.debug("Loaded properties {}", properties);
        return properties;
    }
}
