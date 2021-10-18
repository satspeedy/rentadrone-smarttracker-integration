package com.hha.rentadrone.config;

import io.dapr.client.DaprClient;
import io.dapr.client.DaprClientBuilder;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class DaprConfiguration {

    private static final DaprClientBuilder BUILDER = new DaprClientBuilder();

    public static final String DAPR_HTTP_PORT_ENV_VAR = "DAPR_HTTP_PORT";

    public static final String DAPR_PUBSUB_NAME = "pubsub-kafka-rentadrone";
    public static final String DAPR_PUBSUB_MESSAGE_PARTITION_KEY = "partitionKey";
    public static final String DAPR_PUBSUB_MESSAGE_TTL_IN_SECONDS = "1000";

    public static final String DAPR_STATE_STORE_NAME = "statestore";
    public static final String DAPR_STATE_STORE_KEY_PREFIX = "Tracking:";

    public static final String SMART_TRACKER_APP_ID = "smarttracker-app-id";
    public static final String SMART_TRACKER_URL_PATH_CREATE_TRACKING = "api/trackings";
    public static final String SMART_TRACKER_URL_PATH_GET_A_TRACKING = "api/trackings";

    private static final String PIN_PLACEHOLDER = "YOUR_PIN";

    @Bean
    public DaprClient buildDaprClient() {
        return BUILDER.build();
    }

    /**
     * Should look like <br>
     * http://localhost:3083/v1.0/invoke/smarttracker-app-id/method/api/trackings/-2243967249972640760?pin=YOUR_PIN
     * @param trackingNumber tracking number
     * @return Generated dapr invoke url to get the current tracking
     */
    @SneakyThrows
    public static String generateDaprInvokeUrlGetATracking(Long trackingNumber) {
        String daprHttpPort = System.getenv(DAPR_HTTP_PORT_ENV_VAR);
        if (daprHttpPort == null) {
            throw new IllegalStateException("Environment variable " + DAPR_HTTP_PORT_ENV_VAR + " is missing.");
        }
        String url = "http://localhost:"
                + daprHttpPort
                + "/v1.0/invoke/"
                + SMART_TRACKER_APP_ID
                + "/method/"
                + SMART_TRACKER_URL_PATH_GET_A_TRACKING
                + "/"
                + trackingNumber
                + "?pin=" + PIN_PLACEHOLDER;
        log.info("Generated dapr invoke url to get the tracking: {}", url);
        return url;
    }
}
