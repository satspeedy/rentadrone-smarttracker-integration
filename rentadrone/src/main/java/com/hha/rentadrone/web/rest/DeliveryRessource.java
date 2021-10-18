package com.hha.rentadrone.web.rest;

import com.hha.rentadrone.domain.Drone;
import com.hha.rentadrone.domain.User;
import com.hha.rentadrone.service.*;
import com.hha.rentadrone.web.rest.dto.DeliveryDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * REST controller for managing {@link DeliveryDTO}.
 */
@Slf4j
@RestController
@RequestMapping(value = "/api/shipping")
public class DeliveryRessource {

    private static final int RETURN_FLIGHT_TO_HEAD_OFFICE_IN_MIN = 15;

    private final DeliveryService deliveryService;

    private final DroneService droneService;

    private final UserService userService;

    private final SchedulerService schedulerService;

    public DeliveryRessource(DeliveryService deliveryService, DroneService droneService, UserService userService, SchedulerService schedulerService) {
        this.deliveryService = deliveryService;
        this.droneService = droneService;
        this.userService = userService;
        this.schedulerService = schedulerService;
    }

    /**
     * {@code POST  /deliveries} : Book a delivery.
     *
     * @param dto the delivery to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new delivery, or with status {@code 400 (Bad Request)} if the delivery has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @Operation(summary = "Create a new delivery")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = DeliveryDTO.class))}),
            @ApiResponse(responseCode = "400", description = "Has already an ID",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Couldn't be created",
                    content = @Content)})
    @PostMapping("/deliveries")
    public ResponseEntity<DeliveryDTO> createDelivery(@Valid @RequestBody DeliveryDTO dto) throws URISyntaxException {
        log.info("REST request to save Delivery : {}", StringifyHelper.toJson(dto));

        if (dto.getId() != null) {
            throw new IllegalStateException("A new delivery cannot already have an ID: id-exists");
        }

        if (dto.getPickupLocalDateTime().isBefore(LocalDateTime.now().plusMinutes(1))) {
            throw new IllegalStateException("A new delivery pickup time must be after current time + 1 minute: pickupTimeInFuture");
        }

        Set<Long> availableDroneIds = determineAvailableDrones(null, dto.getPickupLocalDateTime());
        dto.setDroneId(assignAnotherDroneIfDesiredIsNotAvailable(dto.getDroneId(), availableDroneIds));

        DeliveryDTO result = deliveryService.save(dto);

        JobDetail jobDetail = schedulerService.buildJobDetail(result.getId());
        Trigger trigger = schedulerService.buildJobTrigger(jobDetail, result.getPickupLocalDateTime());
        String schedulerJobKey = schedulerService.scheduleJob(jobDetail, trigger);
        result.setSchedulerJobKey(schedulerJobKey);

        result = deliveryService.save(result);

        return ResponseEntity
                .created(new URI("/api/shipping/deliveries/" + result.getId()))
                .body(result);
    }

    private Set<Long> determineAvailableDrones(Long prevCreatedDeliveryId, LocalDateTime desiredPickupDateTime) {
        Set<Long> unavailableDroneIds = new HashSet<>();

        List<DeliveryDTO> remainingDeliveries = deliveryService.findAll()
                .stream()
                .filter(e -> !Objects.equals(prevCreatedDeliveryId, e.getId()))
                .collect(Collectors.toList());

        for (DeliveryDTO delivery : remainingDeliveries) {
            if (!desiredPickupDateTime.isBefore(delivery.getPickupLocalDateTime())
                    &&
                    !desiredPickupDateTime.isAfter(delivery.getEstimatedTimeOfArrival()
                            .plusMinutes(RETURN_FLIGHT_TO_HEAD_OFFICE_IN_MIN))) {
                unavailableDroneIds.add(delivery.getDroneId());
            }
        }

        Set<Long> availableDroneIds = droneService.findAll()
                .stream()
                .map(Drone::getId)
                .filter(id -> !unavailableDroneIds.contains(id))
                .collect(Collectors.toSet());

        if (availableDroneIds.isEmpty()) {
            throw new IllegalStateException("No available drone existing at this time: no-available-drone-at-pickup-time");
        }

        return availableDroneIds;
    }

    private Long assignAnotherDroneIfDesiredIsNotAvailable(Long desiredDroneId, Set<Long> availableDroneIds) {
        if (desiredDroneId == null || !availableDroneIds.contains(desiredDroneId)) {
            return availableDroneIds.iterator().next();
        } else {
            return desiredDroneId;
        }
    }

    /**
     * {@code PUT  /deliveries/:id} : Replace an existing delivery.
     *
     * @param id          the id of the delivery to save.
     * @param deliveryDTO the delivery to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated delivery,
     * or with status {@code 400 (Bad Request)} if the delivery is not valid,
     * or with status {@code 500 (Internal Server Error)} if the delivery couldn't be updated.
     */
    @Operation(summary = "Replace the complete existing delivery")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found and updated",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = DeliveryDTO.class))}),
            @ApiResponse(responseCode = "400", description = "Not valid",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Not found",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Couldn't be updated",
                    content = @Content)})
    @PutMapping("/deliveries/{id}")
    public ResponseEntity<DeliveryDTO> updateDelivery(
            @Parameter(description = "the id of the delivery to update") @PathVariable(value = "id", required = false) final Long id,
            @Valid @RequestBody DeliveryDTO deliveryDTO
    ) {
        log.info("REST request to update Delivery : {}, {}", id, StringifyHelper.toJson(deliveryDTO));
        if (deliveryDTO.getId() == null) {
            throw new IllegalStateException("Invalid delivery id: id-null");
        }
        if (!Objects.equals(id, deliveryDTO.getId())) {
            throw new IllegalStateException("Invalid delivery id: id-invalid");
        }

        if (deliveryDTO.getPickupLocalDateTime().isBefore(LocalDateTime.now().plusMinutes(1))) {
            throw new IllegalStateException("A delivery pickup time must be after current time + 1 minute: pickupTimeInFuture");
        }

        Optional<DeliveryDTO> existing = deliveryService.findDto(id);
        if (existing.isEmpty()) {
            throw new IllegalStateException("Invalid delivery id: id-not-found");
        }
        DeliveryDTO existingDelivery = existing.get();

        Set<Long> availableDroneIds = determineAvailableDrones(deliveryDTO.getId(), deliveryDTO.getPickupLocalDateTime());
        deliveryDTO.setDroneId(assignAnotherDroneIfDesiredIsNotAvailable(existingDelivery.getDroneId(), availableDroneIds));
        if (deliveryDTO.getUserName() == null) {
            deliveryDTO.setUserName(existingDelivery.getUserName());
        }

        schedulerService.deleteJob(existingDelivery.getSchedulerJobKey());
        JobDetail jobDetail = schedulerService.buildJobDetail(deliveryDTO.getId());
        Trigger trigger = schedulerService.buildJobTrigger(jobDetail, deliveryDTO.getPickupLocalDateTime());
        String schedulerJobKey = schedulerService.scheduleJob(jobDetail, trigger);
        deliveryDTO.setSchedulerJobKey(schedulerJobKey);

        DeliveryDTO result = deliveryService.save(deliveryDTO);
        return ResponseEntity
                .ok()
                .body(result);
    }

    /**
     * {@code PATCH  /api/shipping/deliveries/:id} : Partial updates given fields of an existing delivery, field will ignore if it is null
     *
     * @param id          the id of the delivery to save.
     * @param deliveryDTO the delivery to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated delivery,
     * or with status {@code 400 (Bad Request)} if the delivery is not valid,
     * or with status {@code 404 (Not Found)} if the delivery is not found,
     * or with status {@code 500 (Internal Server Error)} if the delivery couldn't be updated.
     */
    @Operation(summary = "Update only given fields of an existing delivery, field will ignore if it is null")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found and updated",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = DeliveryDTO.class))}),
            @ApiResponse(responseCode = "400", description = "Not valid",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Not found",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Couldn't be updated",
                    content = @Content)})
    @PatchMapping(value = "/deliveries/{id}")
    public ResponseEntity<DeliveryDTO> partialUpdateDelivery(
            @Parameter(description = "the id of the delivery to update") @PathVariable(value = "id", required = false) final Long id,
            @NotNull @RequestBody DeliveryDTO deliveryDTO
    ) {
        log.info("REST request to partial update Delivery partially : {}, {}", id, StringifyHelper.toJson(deliveryDTO));

        if (deliveryDTO.getId() == null) {
            throw new IllegalStateException("Invalid delivery id: id-null");
        }

        if (!Objects.equals(id, deliveryDTO.getId())) {
            throw new IllegalStateException("Invalid delivery id: id-invalid");
        }

        Optional<DeliveryDTO> existing = deliveryService.findDto(id);
        if (existing.isEmpty()) {
            throw new IllegalStateException("Invalid delivery id: id-not-found");
        }
        DeliveryDTO existingDelivery = existing.get();

        LocalDateTime pickupLocalDateTime = deliveryDTO.getPickupLocalDateTime() != null
                ? deliveryDTO.getPickupLocalDateTime() : existingDelivery.getPickupLocalDateTime();

        if (pickupLocalDateTime.isBefore(LocalDateTime.now().plusMinutes(1))) {
            throw new IllegalStateException("A delivery pickup time must be after current time + 1 minute: pickup-time-in-the-past");
        }

        Set<Long> availableDroneIds = determineAvailableDrones(deliveryDTO.getId(), pickupLocalDateTime);

        Long desiredDroneId = deliveryDTO.getDroneId() != null ? deliveryDTO.getDroneId() : existingDelivery.getDroneId();
        deliveryDTO.setDroneId(assignAnotherDroneIfDesiredIsNotAvailable(desiredDroneId, availableDroneIds));

        // Only if a new pickup time is given
        if (deliveryDTO.getPickupLocalDateTime() != null) {
            schedulerService.deleteJob(existingDelivery.getSchedulerJobKey());
            JobDetail jobDetail = schedulerService.buildJobDetail(deliveryDTO.getId());
            Trigger trigger = schedulerService.buildJobTrigger(jobDetail, deliveryDTO.getPickupLocalDateTime());
            String schedulerJobKey = schedulerService.scheduleJob(jobDetail, trigger);
            deliveryDTO.setSchedulerJobKey(schedulerJobKey);
        }

        Optional<DeliveryDTO> result = deliveryService.partialUpdate(deliveryDTO);

        return result.map(response -> ResponseEntity.ok().body(response))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    /**
     * {@code GET  /deliveries} : get all the deliveries.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of deliveries in body.
     */
    @Operation(summary = "Get all deliveries")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Return the list of found deliveries in body",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = DeliveryDTO.class))})})
    @GetMapping("/deliveries")
    public ResponseEntity<List<DeliveryDTO>> getAllDeliveries() {
        log.info("REST request to get all Deliveries");
        return ResponseEntity.ok(deliveryService.findAll());
    }

    /**
     * {@code GET  /deliveries} : get all the deliveries related user.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of related deliveries in body.
     */
    @Operation(summary = "Get all deliveries related to user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Return the list of found deliveries in body",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = DeliveryDTO.class))})})
    @GetMapping("/deliveries/user/{userName}")
    public ResponseEntity<List<DeliveryDTO>> getAllDeliveriesByUserName(@Parameter(description = "username of the user to retrieve related deliveries") @PathVariable String userName) {
        log.info("REST request to get all Deliveries related to user");
        Optional<User> user = userService.findOne(userName);
        if (user.isEmpty()) {
            throw new IllegalStateException("User not exists: user-not-exists");
        }
        return ResponseEntity.ok(deliveryService.findAllByUserId(user.get().getId()));
    }

    /**
     * {@code GET  /deliveries/:id} : get the "id" delivery.
     *
     * @param id the id of the delivery to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the delivery, or with status {@code 404 (Not Found)}.
     */
    @Operation(summary = "Get a delivery by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the delivery",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = DeliveryDTO.class))}),
            @ApiResponse(responseCode = "400", description = "Invalid id supplied",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Not found",
                    content = @Content)})
    @GetMapping("/deliveries/{id}")
    public ResponseEntity<DeliveryDTO> getDelivery(@Parameter(description = "id of the delivery to retrieve") @PathVariable Long id) {
        log.info("REST request to get Delivery : {}", id);
        Optional<DeliveryDTO> deliveryDTO = deliveryService.findDto(id);
        return deliveryDTO.map(response -> ResponseEntity.ok().body(response))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    /**
     * {@code DELETE  /deliveries/:id} : delete the "id" delivery.
     *
     * @param id the id of the delivery to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @Operation(summary = "Delete a delivery by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Found and deleted successful",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = DeliveryDTO.class))}),
            @ApiResponse(responseCode = "400", description = "Invalid id supplied",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Not found",
                    content = @Content)})
    @DeleteMapping("/deliveries/{id}")
    public ResponseEntity<Void> deleteDelivery(@Parameter(description = "id of the delivery to delete") @PathVariable Long id) {
        log.info("REST request to delete Delivery : {}", id);
        Optional<DeliveryDTO> optional = deliveryService.findDto(id);
        if (optional.isPresent()) {
            DeliveryDTO dto = optional.get();
            schedulerService.deleteJob(dto.getSchedulerJobKey());
        }
        deliveryService.delete(id);
        return ResponseEntity
                .noContent()
                .build();
    }

}
