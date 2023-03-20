package com.tomasalmeida.dedu.permission.printers.exception;

public class KafkaPrinterException extends RuntimeException {

    public KafkaPrinterException(final String message, final Throwable e) {
        super(message, e);
    }
}
