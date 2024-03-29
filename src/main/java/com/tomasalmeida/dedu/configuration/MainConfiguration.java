package com.tomasalmeida.dedu.configuration;

import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import com.tomasalmeida.dedu.api.system.PropertiesLoader;

public class MainConfiguration {

    @VisibleForTesting
    static final String LOG_LEVEL = "log.level";
    @VisibleForTesting
    static final String DEFAULT_LOG_LEVEL = Level.INFO.toString();

    private final String kafkaConfigPath;
    private final DeduConfiguration deduConfiguration;
    private final String principal;


    private MainConfiguration(@NotNull final String kafkaConfigPath,
                              @NotNull final String deduConfigPath,
                              @Nullable final String principal) throws IOException {
        this.kafkaConfigPath = kafkaConfigPath;
        this.deduConfiguration = DeduConfiguration.build(deduConfigPath);
        this.principal = principal;
        setLogLevel();
    }

    public static MainConfiguration build(@NotNull final String kafkaConfigPath,
                                          @NotNull final String deduConfigPath,
                                          @Nullable final String principal) throws IOException {
        return new MainConfiguration(kafkaConfigPath, deduConfigPath, principal);
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

    @Nullable
    public String getPrincipal() {
        return principal;
    }

    @NotNull
    public Properties getKafkaConfigProperties() throws IOException {
        return PropertiesLoader.loadFromFile(kafkaConfigPath);
    }
}
