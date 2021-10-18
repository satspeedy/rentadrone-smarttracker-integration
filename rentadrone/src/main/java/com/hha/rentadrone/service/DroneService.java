package com.hha.rentadrone.service;

import com.hha.rentadrone.domain.Drone;
import com.hha.rentadrone.messaging.KafkaSender;
import com.hha.rentadrone.messaging.event.DroneChangedEvent;
import com.hha.rentadrone.repository.DroneRepository;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.hha.rentadrone.config.KafkaTopicNames.DRONE_CHANGED_TOPIC;
import static com.hha.rentadrone.config.KafkaTopicNames.DRONE_DELETED_TOPIC;

@Slf4j
@Service
@Transactional
public class DroneService {

    private final DroneRepository droneRepository;

    private final KafkaSender kafkaSender;

    public DroneService(DroneRepository droneRepository, KafkaSender kafkaSender) {
        this.droneRepository = droneRepository;
        this.kafkaSender = kafkaSender;
    }

    /**
     * Save a drone.
     *
     * @param drone the entity to save.
     * @return the persisted entity.
     */
    @SneakyThrows
    public Drone save(Drone drone) {
        log.info("Request to save Drone : {}", StringifyHelper.toJson(drone));
        Drone savedDrone = droneRepository.save(drone);
        kafkaSender.droneChanged(String.valueOf(drone.getId()), mapTo(drone), DRONE_CHANGED_TOPIC);
        return savedDrone;
    }

    private DroneChangedEvent mapTo(Drone drone) {
        return DroneChangedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventDateTime(LocalDateTime.now())
                .droneId(drone.getId())
                .nickName(drone.getNickName())
                .model(drone.getModel())
                .droneStatus(drone.getDroneStatus().name())
                .operationStatus(drone.getOperationStatus().name())
                .image(drone.getImage())
                .imageContentType(drone.getImageContentType())
                .build();
    }

    /**
     * Partially update a drone.
     *
     * @param drone the entity to update partially.
     * @return the persisted entity.
     */
    @SneakyThrows
    public Optional<Drone> partialUpdate(Drone drone) {
        log.info("Request to partially update Drone : {}", StringifyHelper.toJson(drone));

        return droneRepository
                .findById(drone.getId())
                .map(
                        existingDrone -> {
                            if (drone.getNickName() != null) {
                                existingDrone.setNickName(drone.getNickName());
                            }
                            if (drone.getModel() != null) {
                                existingDrone.setModel(drone.getModel());
                            }
                            if (drone.getDroneStatus() != null) {
                                existingDrone.setDroneStatus(drone.getDroneStatus());
                            }
                            if (drone.getOperationStatus() != null) {
                                existingDrone.setOperationStatus(drone.getOperationStatus());
                            }
                            if (drone.getImage() != null) {
                                existingDrone.setImage(drone.getImage());
                            }
                            if (drone.getImageContentType() != null) {
                                existingDrone.setImageContentType(drone.getImageContentType());
                            }

                            return existingDrone;
                        }
                )
                .map(droneRepository::save)
                .map(updatedDrone -> {
                    kafkaSender.droneChanged(String.valueOf(updatedDrone.getId()), mapTo(updatedDrone), DRONE_CHANGED_TOPIC);
                    return updatedDrone;
                });
    }

    /**
     * Get all the drones.
     *
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public List<Drone> findAll() {
        log.info("Request to get all Drones");
        return droneRepository.findAll();
    }

    /**
     * Get one drone by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<Drone> findOne(Long id) {
        log.info("Request to get Drone : {}", id);
        return droneRepository.findById(id);
    }

    /**
     * Delete the drone by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        log.info("Request to delete Drone : {}", id);
        droneRepository.deleteById(id);
        kafkaSender.sendMessage(String.valueOf(id), "{\"id\": " + id + "}", DRONE_DELETED_TOPIC);
    }
}
