package com.hha.dronesim;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@Slf4j
@SpringBootApplication
@EnableKafka
public class DroneSimApplication {
    public static final String ENV_VAR_GOOGLE_API_KEY = "GOOGLE_API_KEY";

    public static void main(String[] args) {
        checkRequiredEnvVariables();
        SpringApplication.run(DroneSimApplication.class, args);
    }

    private static void checkRequiredEnvVariables() {
        if (System.getenv(ENV_VAR_GOOGLE_API_KEY) != null) {
            log.info("All required environment variables are given.");
        } else {
            throw new IllegalStateException("Environment variables are required to set before running this app: "
                    + ENV_VAR_GOOGLE_API_KEY
                    + ".");
        }
    }
}
