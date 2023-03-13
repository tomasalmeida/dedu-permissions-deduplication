package com.tomasalmeida.dedu.configuration;

import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.jetbrains.annotations.NotNull;

import com.tomasalmeida.dedu.api.system.PropertiesLoader;

public class MainConfiguration {

    private static final String LOG_LEVEL = "log.level";
    private static final String DEFAULT_LOG_LEVEL = Level.INFO.toString();

    private final String kafkaConfigPath;
    private final DeduConfiguration deduConfiguration;
    private final String principal;


    public MainConfiguration(@NotNull final String kafkaConfigPath,
                             @NotNull final String deduConfigPath,
                             @NotNull final String principal) throws IOException {
        this.kafkaConfigPath = kafkaConfigPath;
        this.deduConfiguration = DeduConfiguration.build(deduConfigPath);
        this.principal = principal;
        setLogLevel();
    }

    private void setLogLevel() {
        final String logLevel = getDeduPropertyOrDefault(LOG_LEVEL, DEFAULT_LOG_LEVEL);
        final Level level = Level.toLevel(logLevel);
        LogManager.getRootLogger().setLevel(level);
    }

    @NotNull
    public String getDeduPropertyOrDefault(@NotNull final String key, @NotNull final String defaultValue) {
        return deduConfiguration.getPropertyOrDefault(key, defaultValue);
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
