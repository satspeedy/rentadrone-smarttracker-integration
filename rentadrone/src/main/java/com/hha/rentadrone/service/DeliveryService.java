package com.hha.rentadrone.service;

import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsResult;
import com.hha.rentadrone.config.DaprConfiguration;
import com.hha.rentadrone.domain.Delivery;
import com.hha.rentadrone.domain.Drone;
import com.hha.rentadrone.domain.User;
import com.hha.rentadrone.domain.enumeration.DeliveryStatus;
import com.hha.rentadrone.domain.enumeration.DroneStatus;
import com.hha.rentadrone.messaging.KafkaSender;
import com.hha.rentadrone.messaging.event.DeliveryChangedEvent;
import com.hha.rentadrone.messaging.event.DeliveryDeletedEvent;
import com.hha.rentadrone.repository.DeliveryRepository;
import com.hha.rentadrone.repository.DroneRepository;
import com.hha.rentadrone.repository.UserRepository;
import com.hha.rentadrone.web.rest.dto.DeliveryDTO;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.hha.rentadrone.config.TopicNames.DELIVERY_CHANGED_TOPIC;
import static com.hha.rentadrone.config.TopicNames.DELIVERY_DELETED_TOPIC;

@Slf4j
@Service
@Transactional
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;

    private final DroneRepository droneRepository;

    private final UserRepository userRepository;

    private final GoogleMapsService googleMapsService;

    private final KafkaSender kafkaSender;

    public DeliveryService(DeliveryRepository deliveryRepository,
                           DroneRepository droneRepository,
                           UserRepository userRepository, GoogleMapsService googleMapsService,
                           KafkaSender kafkaSender) {
        this.deliveryRepository = deliveryRepository;
        this.droneRepository = droneRepository;
        this.userRepository = userRepository;
        this.googleMapsService = googleMapsService;
        this.kafkaSender = kafkaSender;
    }

    /**
     * Save a delivery.
     *
     * @param deliveryDTO the entity to save.
     * @return the persisted entity.
     */
    @SneakyThrows
    public DeliveryDTO save(DeliveryDTO deliveryDTO) {
        log.info("Request to save Delivery : {}", StringifyHelper.toJson(deliveryDTO));
        Delivery delivery = mapDtoToEntity(deliveryDTO);
        enrichDeliveryWithDrone(delivery, deliveryDTO.getDroneId());
        enrichDeliveryWithUser(delivery, deliveryDTO.getUserName());
        enrichDeliveryWithDirections(delivery);
        Delivery result = deliveryRepository.save(delivery);

        kafkaSender.deliveryChanged(String.valueOf(result.getId()),
                mapEntityToEvent(result), DELIVERY_CHANGED_TOPIC);
        return mapEntityToDto(result);
    }

    /**
     * Map DTO to entity.
     * Except the droneId because for this the referenced drone is determined later and linked with it.
     */
    private Delivery mapDtoToEntity(DeliveryDTO dto) {
        return Delivery.builder()
                .id(dto.getId())
                .startAddress(dto.getStartAddress())
                .endAddress(dto.getEndAddress())
                .pickupLocalDateTime(dto.getPickupLocalDateTime())
                .estimatedTimeOfArrival(dto.getEstimatedTimeOfArrival())
                .deliveryStatus(DeliveryStatus.SCHEDULED)
                .schedulerJobKey(dto.getSchedulerJobKey())
                .trackingNumber(dto.getTrackingNumber())
                .trackingUrl(dto.getTrackingUrl())
                .build();
    }

    private DeliveryDTO mapEntityToDto(Delivery entity) {
        return DeliveryDTO.builder()
                .id(entity.getId())
                .startAddress(entity.getStartAddress())
                .endAddress(entity.getEndAddress())
                .pickupLocalDateTime(entity.getPickupLocalDateTime())
                .estimatedTimeOfArrival(entity.getEstimatedTimeOfArrival())
                .deliveryStatus(entity.getDeliveryStatus().name())
                .schedulerJobKey(entity.getSchedulerJobKey())
                .droneId(entity.getDrone().getId())
                .userName(entity.getUser().getUserName())
                .trackingNumber(entity.getTrackingNumber())
                .trackingUrl(entity.getTrackingUrl())
                .build();
    }

    private DeliveryChangedEvent mapEntityToEvent(Delivery entity) {
        return DeliveryChangedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventDateTime(LocalDateTime.now())
                .deliveryId(entity.getId())
                .startAddress(entity.getStartAddress())
                .endAddress(entity.getEndAddress())
                .startLatitude(entity.getStartLatitude())
                .startLongitude(entity.getStartLongitude())
                .endLatitude(entity.getEndLatitude())
                .endLongitude(entity.getEndLongitude())
                .pickupLocalDateTime(entity.getPickupLocalDateTime())
                .estimatedTimeOfArrival(entity.getEstimatedTimeOfArrival())
                .deliveryStatus(entity.getDeliveryStatus().name())
                .droneId(entity.getDrone().getId())
                .droneNickName(entity.getDrone().getNickName())
                .userId(entity.getUser().getId())
                .userName(entity.getUser().getUserName())
                .trackingNumber(entity.getTrackingNumber())
                .build();
    }

    private void enrichDeliveryWithDrone(Delivery delivery, Long droneId) {
        if (droneId != null) {
            Optional<Drone> referencedDrone = droneRepository.findById(droneId);
            if (referencedDrone.isPresent()) {
                delivery.setDrone(referencedDrone.get());
            } else {
                delivery.setDrone(null);
            }
        }
    }

    private void enrichDeliveryWithUser(Delivery delivery, String userName) {
        if (userName != null) {
            Optional<User> referencedUser = userRepository.findOneByUserName(userName);
            if (referencedUser.isPresent()) {
                delivery.setUser(referencedUser.get());
            } else {
                delivery.setUser(null);
            }
        }
    }

    private void enrichDeliveryWithDirections(Delivery delivery) {
        DirectionsResult directionsResult = googleMapsService.calculateDirections(delivery.getStartAddress(), delivery.getEndAddress());
        DirectionsLeg leg = directionsResult.routes[0].legs[0];
        delivery.setStartLatitude(leg.startLocation.lat);
        delivery.setStartLongitude(leg.startLocation.lng);
        delivery.setEndLatitude(leg.endLocation.lat);
        delivery.setEndLongitude(leg.endLocation.lng);
        delivery.setEstimatedTimeOfArrival(delivery.getPickupLocalDateTime().plusSeconds(leg.duration.inSeconds));
    }

    /**
     * Partially update a delivery.
     *
     * @param dto the dto to update the entity partially.
     * @return the persisted dto.
     */
    @SneakyThrows
    public Optional<DeliveryDTO> partialUpdate(DeliveryDTO dto) {
        log.info("Request to partially update Delivery : {}", StringifyHelper.toJson(dto));

        return deliveryRepository
                .findById(dto.getId())
                .map(entity -> {
                            updateStartAddress(dto, entity);
                            updateEndAddress(dto, entity);
                            updatePickupLocalDateTime(dto, entity);
                            updateDeliveryStatus(dto, entity);
                            updateTrackingNumberAndUrl(dto, entity);
                            updateSchedulerJobKey(dto, entity);
                            enrichDeliveryWithDrone(entity, dto.getDroneId());
                            enrichDeliveryWithUser(entity, dto.getUserName());
                            enrichDeliveryWithDirections(entity);
                            return entity;
                        }
                )
                .map(deliveryRepository::save)
                .map(result -> {
                    kafkaSender.deliveryChanged(String.valueOf(result.getId()), mapEntityToEvent(result), DELIVERY_CHANGED_TOPIC);
                    return mapEntityToDto(result);
                });
    }

    private void updateSchedulerJobKey(DeliveryDTO delivery, Delivery existingDelivery) {
        if (delivery.getSchedulerJobKey() != null) {
            existingDelivery.setSchedulerJobKey(delivery.getSchedulerJobKey());
        }
    }

    private void updateTrackingNumberAndUrl(DeliveryDTO dto, Delivery entity) {
        if (dto.getTrackingNumber() != null) {
            entity.setTrackingNumber(dto.getTrackingNumber());
            entity.setTrackingUrl(DaprConfiguration.generateDaprInvokeUrlGetATracking(dto.getTrackingNumber()));
        }
    }

    private void updateDeliveryStatus(DeliveryDTO delivery, Delivery existingDelivery) {
        if (delivery.getDeliveryStatus() != null) {
            existingDelivery.setDeliveryStatus(DeliveryStatus.valueOf(delivery.getDeliveryStatus()));
        }
    }

    private void updatePickupLocalDateTime(DeliveryDTO delivery, Delivery existingDelivery) {
        if (delivery.getPickupLocalDateTime() != null) {
            existingDelivery.setPickupLocalDateTime(delivery.getPickupLocalDateTime());
        }
    }

    private void updateEndAddress(DeliveryDTO delivery, Delivery existingDelivery) {
        if (delivery.getEndAddress() != null) {
            existingDelivery.setEndAddress(delivery.getEndAddress());
        }
    }

    private void updateStartAddress(DeliveryDTO delivery, Delivery existingDelivery) {
        if (delivery.getStartAddress() != null) {
            existingDelivery.setStartAddress(delivery.getStartAddress());
        }
    }

    /**
     * Get all the delivery.
     *
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public List<DeliveryDTO> findAll() {
        log.info("Request to get all Deliveries");
        return deliveryRepository.findAll().stream().map(this::mapEntityToDto).collect(Collectors.toList());
    }

    /**
     * Get all the related delivery to a user.
     *
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public List<DeliveryDTO> findAllByUserId(Long userId) {
        log.info("Request to get all Deliveries related to a user");
        return deliveryRepository.findAllByUserId(userId).stream().map(this::mapEntityToDto).collect(Collectors.toList());
    }

    /**
     * Get one delivery dto by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<DeliveryDTO> findDto(Long id) {
        return this.find(id).map(this::mapEntityToDto);
    }

    /**
     * Get one delivery by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<Delivery> find(Long id) {
        log.info("Request to get Delivery : {}", id);
        return deliveryRepository.findById(id);
    }

    /**
     * Delete the delivery by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        log.info("Request to delete Delivery : {}", id);
        Optional<Delivery> optFound = deliveryRepository.findById(id);
        if (optFound.isPresent()) {
            deliveryRepository.deleteById(id);
            Delivery delivery = optFound.get();
            DeliveryDeletedEvent deletedEvent = DeliveryDeletedEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventDateTime(LocalDateTime.now())
                    .deliveryId(delivery.getId())
                    .trackingNumber(delivery.getTrackingNumber())
                    .build();
            kafkaSender.deliveryDeleted(deletedEvent.getEventId(), deletedEvent, DELIVERY_DELETED_TOPIC);
        }
    }

    public void updateDeliveryStatusByDroneStatus(Long assignedDroneId, DroneStatus deriveFromDroneStatus, LocalDateTime atDesiredTime) {
        log.info("Request to update Delivery Status with assigned Drone Id {} derived from Drone Status {} and flying at {}", assignedDroneId, deriveFromDroneStatus, atDesiredTime);
        DeliveryStatus deliveryStatus;
        if (deriveFromDroneStatus.equals(DroneStatus.PARKED)) {
            deliveryStatus = DeliveryStatus.COMPLETED;
        } else {
            deliveryStatus = DeliveryStatus.IN_FLIGHT;
        }
        deliveryRepository.updateDeliveryStatusOfAllActiveByDroneId(deliveryStatus, assignedDroneId, atDesiredTime);
    }
}
