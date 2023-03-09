package com.tomasalmeida.dedu.api.kafka;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.junit.jupiter.MockitoExtension;

@Execution(ExecutionMode.CONCURRENT)
@ExtendWith(MockitoExtension.class)
class KafkaAdminExceptionTest {

    @Test
    void shouldExceptionBeCreated() {
        final Exception e = mock(Exception.class);

        KafkaAdminException exception = new KafkaAdminException(e);

        assertEquals(e, exception.getCause());
    }
}