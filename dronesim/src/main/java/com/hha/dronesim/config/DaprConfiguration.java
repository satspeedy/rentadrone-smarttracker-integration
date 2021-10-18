package com.hha.dronesim.config;

import io.dapr.client.DaprClient;
import io.dapr.client.DaprClientBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class DaprConfiguration {

    private static final DaprClientBuilder BUILDER = new DaprClientBuilder();

    public static final String DAPR_BINDING_NAME = "tracking-position-changed-event";
    public static final String DAPR_BINDING_OPERATION = "create";

    @Bean
    public DaprClient buildDaprClient() {
        return BUILDER.build();
    }

}
