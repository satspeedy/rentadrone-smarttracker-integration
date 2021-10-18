package com.hha.rentadrone.web.rest;

import com.hha.rentadrone.domain.Drone;
import com.hha.rentadrone.service.DroneService;
import com.hha.rentadrone.service.StringifyHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * REST controller for managing {@link Drone}.
 */
@Slf4j
@RestController
@RequestMapping(value = "/api")
public class DroneRessource {

    private final DroneService droneService;

    public DroneRessource(DroneService droneService) {
        this.droneService = droneService;
    }

    /**
     * {@code POST  /drones} : Create a new drone.
     *
     * @param drone the drone to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new drone, or with status {@code 400 (Bad Request)} if the drone has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @Operation(summary = "Create a new drone")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Drone.class)) }),
            @ApiResponse(responseCode = "400", description = "Has already an ID",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Couldn't be created",
                    content = @Content) })
    @PostMapping("/drones")
    public ResponseEntity<Drone> createDrone(@Valid @RequestBody Drone drone) throws URISyntaxException {
        log.info("REST request to save Drone : {}", StringifyHelper.toJson(drone));
        if (drone.getId() != null) {
            throw new IllegalStateException("A new drone cannot already have an ID: idexists");
        }
        Drone result = droneService.save(drone);
        return ResponseEntity
                .created(new URI("/api/drones/" + result.getId()))
                .body(result);
    }

    /**
     * {@code PUT  /drones/:id} : Updates an existing drone.
     *
     * @param id    the id of the drone to save.
     * @param drone the drone to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated drone,
     * or with status {@code 400 (Bad Request)} if the drone is not valid,
     * or with status {@code 500 (Internal Server Error)} if the drone couldn't be updated.
     */
    @Operation(summary = "Replace the complete existing drone")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found and updated",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Drone.class)) }),
            @ApiResponse(responseCode = "400", description = "Not valid",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Not found",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Couldn't be updated",
                    content = @Content) })
    @PutMapping("/drones/{id}")
    public ResponseEntity<Drone> updateDrone(
            @Parameter(description = "the id of the drone to update") @PathVariable(value = "id", required = false) final Long id,
            @Valid @RequestBody Drone drone) {
        log.info("REST request to update Drone : {}, {}", id, StringifyHelper.toJson(drone));
        checkDroneId(id, drone);

        Drone result = droneService.save(drone);
        return ResponseEntity
                .ok()
                .body(result);
    }

    private void checkDroneId(Long droneIdPathVariable, @RequestBody @Valid Drone droneRequestBody) {
        if (droneRequestBody.getId() == null) {
            throw new IllegalStateException("Invalid drone id: idnull");
        }
        if (!Objects.equals(droneIdPathVariable, droneRequestBody.getId())) {
            throw new IllegalStateException("Invalid drone id: idinvalid");
        }
        if (droneService.findOne(droneIdPathVariable).isEmpty()) {
            throw new IllegalStateException("Invalid drone id: idnotfound");
        }
    }

    /**
     * {@code PATCH  /drones/:id} : Partial updates given fields of an existing drone, field will ignore if it is null
     *
     * @param id    the id of the drone to save.
     * @param drone the drone to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated drone,
     * or with status {@code 400 (Bad Request)} if the drone is not valid,
     * or with status {@code 404 (Not Found)} if the drone is not found,
     * or with status {@code 500 (Internal Server Error)} if the drone couldn't be updated.
     */
    @Operation(summary = "Update only given fields of an existing drone, field will ignore if it is null")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found and updated",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Drone.class)) }),
            @ApiResponse(responseCode = "400", description = "Not valid",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Not found",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Couldn't be updated",
                    content = @Content) })
    @PatchMapping(value = "/drones/{id}")
    public ResponseEntity<Drone> partialUpdateDrone(
            @Parameter(description = "the id of the drone to update") @PathVariable(value = "id", required = false) final Long id,
            @NotNull @RequestBody Drone drone) {
        log.info("REST request to partial update Drone partially : {}, {}", id, StringifyHelper.toJson(drone));
        checkDroneId(id, drone);

        Optional<Drone> result = droneService.partialUpdate(drone);

        return result.map(response -> ResponseEntity.ok().body(response))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    /**
     * {@code GET  /drones} : get all the drones.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of drones in body.
     */
    @Operation(summary = "Get all drones")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Return the list of found drones in body",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Drone.class)) })})
    @GetMapping("/drones")
    public ResponseEntity<List<Drone>> getAllDrones() {
        log.info("REST request to get all Drones");
        return ResponseEntity.ok(droneService.findAll());
    }

    /**
     * {@code GET  /drones/:id} : get the "id" drone.
     *
     * @param id the id of the drone to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the drone, or with status {@code 404 (Not Found)}.
     */
    @Operation(summary = "Get a drone by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the drone",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Drone.class)) }),
            @ApiResponse(responseCode = "400", description = "Invalid id supplied",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Not found",
                    content = @Content) })
    @GetMapping("/drones/{id}")
    public ResponseEntity<Drone> getDrone(@Parameter(description = "id of the drone to retrieve") @PathVariable Long id) {
        log.info("REST request to get Drone : {}", id);
        Optional<Drone> drone = droneService.findOne(id);
        return drone.map(response -> ResponseEntity.ok().body(response))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    /**
     * {@code DELETE  /drones/:id} : delete the "id" drone.
     *
     * @param id the id of the drone to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @Operation(summary = "Delete a drone by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Found and deleted successful",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Drone.class)) }),
            @ApiResponse(responseCode = "400", description = "Invalid id supplied",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Not found",
                    content = @Content) })
    @DeleteMapping("/drones/{id}")
    public ResponseEntity<Void> deleteDrone(@Parameter(description = "id of the drone to delete") @PathVariable Long id) {
        log.info("REST request to delete Drone : {}", id);
        droneService.delete(id);
        return ResponseEntity
                .noContent()
                .build();
    }

}
