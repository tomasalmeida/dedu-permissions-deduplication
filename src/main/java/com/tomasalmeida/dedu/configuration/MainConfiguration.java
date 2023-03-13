package com.tomasalmeida.dedu.configuration;

import java.io.IOException;
import java.util.Properties;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.tomasalmeida.dedu.api.system.PropertiesLoader;

public class MainConfiguration {

    private final String kafkaConfigPath;
    private final DeduConfiguration deduConfiguration;
    private final String principal;


    public MainConfiguration(@NotNull final String kafkaConfigPath,
                             @NotNull final String deduConfigPath,
                             @NotNull final String principal) throws IOException {
        this.kafkaConfigPath = kafkaConfigPath;
        this.deduConfiguration = DeduConfiguration.build(deduConfigPath);
        this.principal = principal;
    }

    @Nullable
    public String getDeduProperty(@NotNull final String key) {
        return deduConfiguration.getProperty(key);
    }

    @NotNull
    public String getPrincipal() {
        return principal;
    }

    @NotNull
    public Properties getKafkaConfigProperties() throws IOException {
        return PropertiesLoader.loadFromFile(kafkaConfigPath);
    }
}
