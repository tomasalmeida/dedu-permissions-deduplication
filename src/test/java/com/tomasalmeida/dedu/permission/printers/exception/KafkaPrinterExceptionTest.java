package com.tomasalmeida.dedu.permission.printers.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.junit.jupiter.MockitoExtension;

@Execution(ExecutionMode.CONCURRENT)
@ExtendWith(MockitoExtension.class)
class KafkaPrinterExceptionTest {

    public static final String ERROR_MESSAGE = "Error message";
    public static final String IO_MESSAGE = "io";

    @Test
    void shouldCreateException() {
        final KafkaPrinterException exception = new KafkaPrinterException(ERROR_MESSAGE, new IOException(IO_MESSAGE));

        assertEquals(ERROR_MESSAGE, exception.getMessage());
        assertEquals(IO_MESSAGE, exception.getCause().getMessage());
    }
}