package com.hha.rentadrone.web.rest;

import com.hha.rentadrone.domain.User;
import com.hha.rentadrone.service.StringifyHelper;
import com.hha.rentadrone.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
 * REST controller for managing {@link User}.
 */
@Slf4j
@RestController
@RequestMapping(value = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
public class UserRessource {

    private final UserService userService;

    public UserRessource(UserService userService) {
        this.userService = userService;
    }

    /**
     * {@code POST  /users} : Create a new user.
     *
     * @param user the user to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new user, or with status {@code 400 (Bad Request)} if the user has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @Operation(summary = "Create a new user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = User.class)) }),
            @ApiResponse(responseCode = "400", description = "Has already an ID",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Couldn't be created",
                    content = @Content) })
    @PostMapping("/users")
    public ResponseEntity<User> createUser(@Valid @RequestBody User user) throws URISyntaxException {
        log.info("REST request to save User : {}", StringifyHelper.toJson(user));
        if (user.getId() != null) {
            throw new IllegalStateException("A new user cannot already have an ID: idexists");
        }
        User result = userService.save(user);
        return ResponseEntity
                .created(new URI("/api/users/" + result.getId()))
                .body(result);
    }

    /**
     * {@code PUT  /users/:id} : Updates an existing user.
     *
     * @param id    the id of the user to save.
     * @param user the user to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated user,
     * or with status {@code 400 (Bad Request)} if the user is not valid,
     * or with status {@code 500 (Internal Server Error)} if the user couldn't be updated.
     */
    @Operation(summary = "Replace the complete existing user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found and updated",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = User.class)) }),
            @ApiResponse(responseCode = "400", description = "Not valid",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Not found",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Couldn't be updated",
                    content = @Content) })
    @PutMapping("/users/{id}")
    public ResponseEntity<User> updateUser(
            @Parameter(description = "the id of the user to update") @PathVariable(value = "id", required = false) final Long id,
            @Valid @RequestBody User user) {
        log.info("REST request to update User : {}, {}", id, StringifyHelper.toJson(user));
        checkUserId(id, user);

        User result = userService.save(user);
        return ResponseEntity
                .ok()
                .body(result);
    }

    private void checkUserId(Long userIdPathVariable, @RequestBody @Valid User userRequestBody) {
        if (userRequestBody.getId() == null) {
            throw new IllegalStateException("Invalid user id: idnull");
        }
        if (!Objects.equals(userIdPathVariable, userRequestBody.getId())) {
            throw new IllegalStateException("Invalid user id: idinvalid");
        }
        if (userService.findOne(userIdPathVariable).isEmpty()) {
            throw new IllegalStateException("Invalid user id: idnotfound");
        }
    }

    /**
     * {@code PATCH  /users/:id} : Partial updates given fields of an existing user, field will ignore if it is null
     *
     * @param id    the id of the user to save.
     * @param user the user to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated user,
     * or with status {@code 400 (Bad Request)} if the user is not valid,
     * or with status {@code 404 (Not Found)} if the user is not found,
     * or with status {@code 500 (Internal Server Error)} if the user couldn't be updated.
     */
    @Operation(summary = "Update only given fields of an existing user, field will ignore if it is null")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found and updated",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = User.class)) }),
            @ApiResponse(responseCode = "400", description = "Not valid",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Not found",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Couldn't be updated",
                    content = @Content) })
    @PatchMapping(value = "/users/{id}")
    public ResponseEntity<User> partialUpdateUser(
            @Parameter(description = "the id of the user to update") @PathVariable(value = "id", required = false) final Long id,
            @NotNull @RequestBody User user) {
        log.info("REST request to partial update User partially : {}, {}", id, StringifyHelper.toJson(user));
        checkUserId(id, user);

        Optional<User> result = userService.partialUpdate(user);

        return result.map(response -> ResponseEntity.ok().body(response))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    /**
     * {@code GET  /users} : get all the users.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of users in body.
     */
    @Operation(summary = "Get all users")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Return the list of found users in body",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = User.class)) })})
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        log.info("REST request to get all Users");
        return ResponseEntity.ok(userService.findAll());
    }

    /**
     * {@code GET  /users/:id} : get the "id" user.
     *
     * @param id the id of the user to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the user, or with status {@code 404 (Not Found)}.
     */
    @Operation(summary = "Get a user by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the user",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = User.class)) }),
            @ApiResponse(responseCode = "400", description = "Invalid id supplied",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Not found",
                    content = @Content) })
    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUser(@Parameter(description = "id of the user to retrieve") @PathVariable Long id) {
        log.info("REST request to get User : {}", id);
        Optional<User> user = userService.findOne(id);
        return user.map(response -> ResponseEntity.ok().body(response))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    /**
     * {@code DELETE  /users/:id} : delete the "id" user.
     *
     * @param id the id of the user to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @Operation(summary = "Delete a user by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Found and deleted successful",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = User.class)) }),
            @ApiResponse(responseCode = "400", description = "Invalid id supplied",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Not found",
                    content = @Content) })
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@Parameter(description = "id of the user to delete") @PathVariable Long id) {
        log.info("REST request to delete User : {}", id);
        userService.delete(id);
        return ResponseEntity
                .noContent()
                .build();
    }

}
