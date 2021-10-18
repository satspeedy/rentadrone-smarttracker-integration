package com.hha.dronesim.service;

import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsStep;
import com.hha.dronesim.domain.DeliveryRoute;
import com.hha.dronesim.domain.DeliveryRouteStep;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;

@Slf4j
@Service
@Transactional
public class GoogleMapsService {

    private final GeoApiContext geoApiContext;

    public GoogleMapsService(GeoApiContext geoApiContext) {
        this.geoApiContext = geoApiContext;
    }

    public void enrichDeliveryRouteWithSteps(@NotNull DeliveryRoute deliveryRoute, String startAddress, String endAddress) {
        DirectionsResult directionsResult = calculateDirections(startAddress, endAddress);
        DirectionsLeg leg = directionsResult.routes[0].legs[0];
        for (DirectionsStep step : leg.steps) {
            deliveryRoute.getDeliveryRouteSteps().add(DeliveryRouteStep.builder()
                    .latitude(step.startLocation.lat)
                    .longitude(step.startLocation.lng)
                    .build()
            );
        }
    }

    @SneakyThrows
    private DirectionsResult calculateDirections(String startAddress, String endAddress) {
        return DirectionsApi.getDirections(geoApiContext,
                startAddress,
                endAddress).await();
    }

}
