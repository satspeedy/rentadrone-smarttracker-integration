package com.hha.dronesim.service;

import com.hha.dronesim.domain.DeliveryRoute;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
public class DeliveryRouteService {

    private final GoogleMapsService googleMapsService;

    private final DroneService droneService;

    public DeliveryRouteService(GoogleMapsService googleMapsService, DroneService droneService) {
        this.googleMapsService = googleMapsService;
        this.droneService = droneService;
    }

    public void startDelivery(DeliveryRoute deliveryRoute) {
        googleMapsService.enrichDeliveryRouteWithSteps(deliveryRoute, deliveryRoute.getStartAddress(), deliveryRoute.getEndAddress());
        new WorkerThread(deliveryRoute, droneService).start();
    }

}
