package com.tomasalmeida.dedu.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;

import java.io.IOException;
import java.util.Properties;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.tomasalmeida.dedu.api.system.PropertiesLoader;

@Execution(ExecutionMode.CONCURRENT)
@ExtendWith(MockitoExtension.class)
class DeduConfigurationTest {

    private static final String PROPERTY_1 = "property-1";
    private static final String PROPERTY_2 = "property-2";
    private static final String VALUE_1 = "value-1";
    private static final String DEFAULT_VALUE = "value-2";
    private static final String CONFIG_PATH = "/etc/a";


    private Properties properties;

    private MockedStatic<PropertiesLoader> propertiesLoaderMocked;

    @BeforeEach
    void givenLoaderIsMocked() {
        propertiesLoaderMocked = Mockito.mockStatic(PropertiesLoader.class);
    }

    @AfterEach
    void thenLoaderIsClosed() {
        propertiesLoaderMocked.close();
    }

    @Test
    void shouldReturnProperty() throws IOException {
        givenPropertiesAreCreated();
        givenPropertyIsLoaded();

        final DeduConfiguration deduConfiguration = DeduConfiguration.build(CONFIG_PATH);

        assertEquals(VALUE_1, deduConfiguration.getPropertyOrDefault(PROPERTY_1, DEFAULT_VALUE));
    }

    @Test
    void shouldReturnDefaultValue() throws IOException {
        givenPropertiesAreCreated();
        givenPropertyIsLoaded();

        final DeduConfiguration deduConfiguration = DeduConfiguration.build(CONFIG_PATH);

        assertEquals(DEFAULT_VALUE, deduConfiguration.getPropertyOrDefault(PROPERTY_2, DEFAULT_VALUE));
    }

    void givenPropertiesAreCreated() {
        properties = new Properties();
        propertiesLoaderMocked.when(() -> PropertiesLoader.loadFromFile(anyString()))
                        .thenReturn(properties);
    }

    void givenPropertyIsLoaded() {
        properties.put(PROPERTY_1, VALUE_1);
    }
}