package com.tomasalmeida.dedu.configuration;

import java.io.IOException;
import java.util.Properties;

import org.jetbrains.annotations.NotNull;

import com.tomasalmeida.dedu.api.system.PropertiesLoader;

class DeduConfiguration {

    private final Properties properties;

    private DeduConfiguration(@NotNull final String configFile) throws IOException {
        properties = PropertiesLoader.loadFromFile(configFile);
    }

    @NotNull
    public static DeduConfiguration build(@NotNull final String configFile) throws IOException {
        return new DeduConfiguration(configFile);
    }

    @NotNull
    public String getPropertyOrDefault(@NotNull final String key, @NotNull final String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
}
