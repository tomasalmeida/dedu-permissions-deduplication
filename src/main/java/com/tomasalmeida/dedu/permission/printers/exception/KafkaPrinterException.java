package com.tomasalmeida.dedu.permission.printers.exception;

import org.jetbrains.annotations.NotNull;

public class KafkaPrinterException extends RuntimeException {

    public KafkaPrinterException(@NotNull final String message,
                                 @NotNull final Throwable e) {
        super(message, e);
    }
}
