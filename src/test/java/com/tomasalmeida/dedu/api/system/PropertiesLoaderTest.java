package com.tomasalmeida.dedu.api.system;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.util.Properties;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.junit.jupiter.MockitoExtension;

@Execution(ExecutionMode.CONCURRENT)
@ExtendWith(MockitoExtension.class)
class PropertiesLoaderTest {

    private static final String FILE_CONFIG_PATH = "src/test/resources/config.properties";
    private static final String INVALID_CONFIG_PATH = "src/test/resources/no.exist";

    private Properties properties;

    @BeforeEach
    void givenPropertiesAreLoaded() throws IOException {
        properties = PropertiesLoader.loadFromFile(FILE_CONFIG_PATH);
    }

    @Test
    void shouldFailIfFileDoesNotExit() {
        assertThrows(IOException.class, () -> PropertiesLoader.loadFromFile(INVALID_CONFIG_PATH));
    }

    @ParameterizedTest
    @CsvSource({
            "value1, 1",
            "value2, 2",
            "value3, 3"
    })
    void shouldReturnProperties(final String key, final String value) {
        assertEquals(value, properties.getProperty(key));
    }
}