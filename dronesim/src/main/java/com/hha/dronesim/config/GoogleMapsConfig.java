package com.hha.dronesim.config;

import com.google.maps.GeoApiContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import static com.hha.dronesim.DroneSimApplication.ENV_VAR_GOOGLE_API_KEY;

@Slf4j
@Configuration
public class GoogleMapsConfig {

    @Value("${" + ENV_VAR_GOOGLE_API_KEY + "}")
    private String googleApiKey;

    @Bean
    @Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
    public GeoApiContext geoApiContext() {
        return new GeoApiContext.Builder()
                .apiKey(googleApiKey)
                .build();
    }

}
