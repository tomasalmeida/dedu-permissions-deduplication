package com.tomasalmeida.dedu;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Configuration {

    private final String kafkaConfig;
    private final String principal;

}
