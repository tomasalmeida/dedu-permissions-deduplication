package com.tomasalmeida.dedu.api.kafka;

public class KafkaAdminException extends RuntimeException {

    public KafkaAdminException(final Exception e) {
        super(e);
    }
}
