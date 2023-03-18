package com.tomasalmeida.dedu.configuration;

import static com.tomasalmeida.dedu.configuration.MainConfiguration.DEFAULT_LOG_LEVEL;
import static com.tomasalmeida.dedu.configuration.MainConfiguration.LOG_LEVEL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.apache.log4j.Level;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@Execution(ExecutionMode.CONCURRENT)
@ExtendWith(MockitoExtension.class)
class MainConfigurationTest {

    private static final String PROPERTY_1 = "p1";
    private static final String VALUE_1 = "v1";
    private static final String DEFAULT_VALUE = "dv";

    private static final String KAFKA_PATH = "/path/to/kafka";
    private static final String DEDU_PATH = "/path/to/dedu";
    private static final String PRINCIPAL = "principal";

    @Mock
    private DeduConfiguration deduConfiguration;

    private MockedStatic<DeduConfiguration> deduConfigurationMocked;
    private MainConfiguration mainConfiguration;

    @BeforeEach
    void givenDeduConfigurationIsMocked() {
        deduConfigurationMocked = Mockito.mockStatic(DeduConfiguration.class);
        deduConfigurationMocked.when(() -> DeduConfiguration.build(anyString()))
                .thenReturn(deduConfiguration);
    }

    @AfterEach
    void givenDeduConfigurationIsClosed() {
        deduConfigurationMocked.close();
    }

    @Test
    void shouldGetPropertyFromDeduConfiguration() throws IOException {
        givenDeduConfigurationReturnsPropertyValue();
        givenDeduConfigurationLogIsCompleted();
        givenConfigurationIsCreated();

        assertEquals(VALUE_1, mainConfiguration.getDeduPropertyOrDefault(PROPERTY_1, DEFAULT_VALUE));
    }

    @Test
    void shouldGetPrincipalFromMainConfiguration() throws IOException {
        givenDeduConfigurationLogIsCompleted();
        givenConfigurationIsCreated();

        assertEquals(PRINCIPAL, mainConfiguration.getPrincipal());
    }

    private void givenConfigurationIsCreated() throws IOException {
        mainConfiguration = MainConfiguration.build(KAFKA_PATH, DEDU_PATH, PRINCIPAL);
    }

    void givenDeduConfigurationLogIsCompleted() {
        when(deduConfiguration.getPropertyOrDefault(LOG_LEVEL, DEFAULT_LOG_LEVEL)).thenReturn(Level.DEBUG.toString());
    }

    void givenDeduConfigurationReturnsPropertyValue() {
        when(deduConfiguration.getPropertyOrDefault(PROPERTY_1, DEFAULT_VALUE)).thenReturn(VALUE_1);
    }
}