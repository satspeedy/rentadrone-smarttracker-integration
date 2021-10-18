package com.hha.rentadrone.service;

import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsResult;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
public class GoogleMapsService {

    private final GeoApiContext geoApiContext;

    public GoogleMapsService(GeoApiContext geoApiContext) {
        this.geoApiContext = geoApiContext;
    }

    @SneakyThrows
    public DirectionsResult calculateDirections(String startAddress, String endAddress) {
        return DirectionsApi.getDirections(geoApiContext,
                startAddress,
                endAddress).await();
    }
}
