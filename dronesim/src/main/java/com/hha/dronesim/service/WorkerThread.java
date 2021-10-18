package com.hha.dronesim.service;

import com.hha.dronesim.domain.DeliveryRoute;
import com.hha.dronesim.domain.DeliveryRouteStep;
import com.hha.dronesim.messaging.event.enumeration.TrackingSignal;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static com.hha.dronesim.domain.DeliveryRoute.LATITUDE_EIFFEL_TOWER;
import static com.hha.dronesim.domain.DeliveryRoute.LONGITUDE_EIFFEL_TOWER;
import static com.hha.dronesim.domain.enumeration.DroneStatus.*;

@Slf4j
@Component
@Scope("prototype")
public class WorkerThread extends Thread {

    private final DeliveryRoute deliveryRoute;

    private final DroneService droneService;

    public WorkerThread(DeliveryRoute deliveryRoute, DroneService droneService) {
        this.deliveryRoute = deliveryRoute;
        this.droneService = droneService;
        this.setName("delivery-" + deliveryRoute.getDeliveryId());
    }

    @Override
    public void run() {
        String droneId = String.valueOf(deliveryRoute.getDroneId());
        String droneNickName = deliveryRoute.getDroneNickName();
        String trackingNumber = String.valueOf(deliveryRoute.getTrackingNumber());

        flyingFromHeadOfficeToStartAddress(droneId, droneNickName, trackingNumber);
        flyingFromStartAddressToEndAddress(droneId, droneNickName, trackingNumber);
        flyingFromEndAddressToHeadOffice(droneId, droneNickName, trackingNumber);
        arrivedAtHeadOfficeForParking(droneId, droneNickName, trackingNumber);
        interruptThread();
    }

    @SneakyThrows
    private void flyingFromHeadOfficeToStartAddress(String droneId, String droneNickName, String trackingNumber) {
        deliveryRoute.setCurrentDroneStatus(IN_FLIGHT_TO_START_ADDRESS);
        deliveryRoute.setCurrentDronePosition(DeliveryRouteStep.builder()
                .latitude(LATITUDE_EIFFEL_TOWER)
                .longitude(LONGITUDE_EIFFEL_TOWER)
                .build());

        droneService.publishDronePosition(trackingNumber,
                TrackingSignal.START,
                LATITUDE_EIFFEL_TOWER,
                LONGITUDE_EIFFEL_TOWER);

        droneService.publishDroneStatus(droneId,
                droneNickName, deliveryRoute.getCurrentDroneStatus());

        log.info("Thread {}: {} is flying to start address at {} with Latitude & Longitude: {},{}.",
                this.getName(),
                droneNickName,
                deliveryRoute.getStartAddress(),
                deliveryRoute.getStartLatitude(),
                deliveryRoute.getStartLongitude());

        Thread.sleep((long) (Math.random() * 1000));
    }

    @SneakyThrows
    private void flyingFromStartAddressToEndAddress(String droneId, String droneNickName, String trackingNumber) {
        deliveryRoute.setCurrentDroneStatus(IN_FLIGHT_TO_END_ADDRESS);
        deliveryRoute.setCurrentDronePosition(DeliveryRouteStep.builder()
                .latitude(deliveryRoute.getStartLatitude())
                .longitude(deliveryRoute.getStartLongitude())
                .build());

        droneService.publishDronePosition(trackingNumber,
                null,
                deliveryRoute.getStartLatitude(),
                deliveryRoute.getStartLongitude());

        droneService.publishDroneStatus(droneId,
                droneNickName, deliveryRoute.getCurrentDroneStatus());

        log.info("Thread {}: {} is flying to end address at {} with Latitude & Longitude: {},{}.", this.getName(),
                droneNickName,
                deliveryRoute.getEndAddress(),
                deliveryRoute.getEndLatitude(),
                deliveryRoute.getEndLongitude());

        log.info("Thread {}: {}'s route contains {} steps", this.getName(),
                droneNickName,
                deliveryRoute.getDeliveryRouteSteps().size());
        Thread.sleep(5000);

        for (DeliveryRouteStep step : deliveryRoute.getDeliveryRouteSteps()) {
            DeliveryRouteStep position = DeliveryRouteStep.builder()
                    .latitude(step.getLatitude())
                    .longitude(step.getLongitude())
                    .build();
            deliveryRoute.setCurrentDronePosition(position);
            droneService.publishDronePosition(trackingNumber,
                    null,
                    position.getLatitude(),
                    position.getLongitude());
            log.info("Thread {}: {}'s current position is Latitude & Longitude: {},{}.",
                    this.getName(),
                    droneNickName,
                    position.getLatitude(),
                    position.getLongitude());
            Thread.sleep(3000);
        }

        log.info("Thread {}: {} has successfully delivered to end address.",
                this.getName(),
                droneNickName);
    }

    @SneakyThrows
    private void flyingFromEndAddressToHeadOffice(String droneId, String droneNickName, String trackingNumber) {
        deliveryRoute.setCurrentDroneStatus(IN_FLIGHT_TO_HEAD_OFFICE);
        deliveryRoute.setCurrentDronePosition(DeliveryRouteStep.builder()
                .latitude(deliveryRoute.getEndLatitude())
                .longitude(deliveryRoute.getEndLongitude())
                .build());

        droneService.publishDronePosition(trackingNumber,
                null,
                deliveryRoute.getEndLatitude(),
                deliveryRoute.getEndLongitude());

        droneService.publishDroneStatus(droneId,
                droneNickName, deliveryRoute.getCurrentDroneStatus());

        log.info("Thread {}: {} is flying to head office.", this.getName(),
                droneNickName);

        Thread.sleep((long) (Math.random() * 1000));
    }

    @SneakyThrows
    private void arrivedAtHeadOfficeForParking(String droneId, String droneNickName, String trackingNumber) {
        deliveryRoute.setCurrentDroneStatus(PARKED);
        deliveryRoute.setCurrentDronePosition(DeliveryRouteStep.builder()
                .latitude(LATITUDE_EIFFEL_TOWER)
                .longitude(LONGITUDE_EIFFEL_TOWER)
                .build());

        droneService.publishDronePosition(trackingNumber,
                TrackingSignal.END,
                LATITUDE_EIFFEL_TOWER,
                LONGITUDE_EIFFEL_TOWER);

        droneService.publishDroneStatus(droneId,
                droneNickName, deliveryRoute.getCurrentDroneStatus());

        log.info("Thread {}: {} arrived at head office.", this.getName(),
                droneNickName);

        Thread.sleep((long) (Math.random() * 1000));
    }

    private void interruptThread() {
        this.interrupt();
        if (this.isInterrupted()) {
            log.info("Thread {}: delivery ended.", this.getName());
        } else {
            log.info("Thread {} is running.", this.getName());
        }
    }

}
