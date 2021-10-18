package com.hha.rentadrone;

import com.github.javafaker.Faker;
import com.hha.rentadrone.domain.Drone;
import com.hha.rentadrone.domain.User;
import com.hha.rentadrone.domain.enumeration.DroneStatus;
import com.hha.rentadrone.domain.enumeration.OperationStatus;
import com.hha.rentadrone.messaging.DaprPubSubPublisher;
import com.hha.rentadrone.repository.DroneRepository;
import com.hha.rentadrone.repository.UserRepository;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.annotation.EnableKafka;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.hha.rentadrone.config.TopicNames.TESTING_TOPIC;

@Slf4j
@SpringBootApplication
@EnableKafka
public class RentADroneApplication {

    private static final Faker FAKER = new Faker();

    private static final String USER_SUPERADMIN = "superadmin";
    private static final String USER_ADMIN = "admin";

    public static final String ENV_VAR_AZURE_CLIENT_ID = "AZURE_CLIENT_ID";
    public static final String ENV_VAR_AZURE_CLIENT_SECRET = "AZURE_CLIENT_SECRET";
    public static final String ENV_VAR_AZURE_TENANT_ID = "AZURE_TENANT_ID";
    public static final String ENV_VAR_AZURE_VAULT_URL = "AZURE_VAULT_URL";
    public static final String ENV_VAR_GOOGLE_API_KEY = "GOOGLE_API_KEY";

    @Autowired
    private DroneRepository droneRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DaprPubSubPublisher daprPubSubPublisher;

    public static void main(String[] args) {
        checkRequiredEnvVariables();
        SpringApplication.run(RentADroneApplication.class, args);
    }

    private static void checkRequiredEnvVariables() {
        if (isEnvVarSet(ENV_VAR_AZURE_CLIENT_ID) && isEnvVarSet(ENV_VAR_AZURE_CLIENT_SECRET) &&
                isEnvVarSet(ENV_VAR_AZURE_TENANT_ID) && isEnvVarSet(ENV_VAR_AZURE_VAULT_URL) &&
                isEnvVarSet(ENV_VAR_GOOGLE_API_KEY)) {
            log.info("All required environment variables are given.");
        } else {
            throw new IllegalStateException("Environment variables are required to set before running this app: "
                    + ENV_VAR_AZURE_CLIENT_ID
                    + ", "
                    + ENV_VAR_AZURE_CLIENT_SECRET
                    + ", "
                    + ENV_VAR_AZURE_TENANT_ID
                    + ", "
                    + ENV_VAR_AZURE_VAULT_URL
                    + ", "
                    + ENV_VAR_GOOGLE_API_KEY
                    + ".");
        }
    }

    private static boolean isEnvVarSet(String envVar) {
        return System.getenv(envVar) != null;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void runAfterStartup() {
        addDronesToDB();
        addAdminUsersToDB();
        exchangePubSubTestEvents(10);
    }

    @SneakyThrows
    private void exchangePubSubTestEvents(int count) {
        for (int i = 0; i < count; i++) {
            String key = UUID.randomUUID().toString();
            String message = String.format("This is message #%d with key #%s", i, key);
            daprPubSubPublisher.publishEvent(
                    TESTING_TOPIC,
                    key,
                    message,
                    Collections.emptyMap());
        }
        log.info("Done.");
    }

    private void addAdminUsersToDB() {
        addUserToDB(USER_SUPERADMIN, "Susan Super Admin");
        addUserToDB(USER_ADMIN, "Adam Admin");
    }

    private void addUserToDB(String userName, String fullName) {
        Optional<User> optUser = userRepository.findOneByUserName(userName);
        if (optUser.isEmpty()) {
            userRepository.save(User.builder().userName(userName).name(fullName).build());
        }
    }

    private void addDronesToDB() {
        List<Drone> allDrones = this.droneRepository.findAll();
        log.info("Number of drones: " + allDrones.size());

        if (allDrones.size() < 10) {
            int quantity = determineMissingQuantity(allDrones.size());
            for (int i = quantity; i > 0; i--) {
                addDroneToDB();
            }
        }
        allDrones = this.droneRepository.findAll();
        log.info("New number of drones: " + allDrones.size());
    }

    private void addDroneToDB() {
        String nickName = FAKER.superhero().prefix() + FAKER.name().firstName() + FAKER.address().buildingNumber();
        String model = FAKER.aviation().aircraft();

        Drone drone = Drone.builder()
                .nickName(nickName)
                .model(model)
                .droneStatus(DroneStatus.PARKED)
                .operationStatus(OperationStatus.OK)
                .build();
        log.info("Saving new Drone with name {}...", drone.getNickName());
        this.droneRepository.save(drone);
    }

    private int determineMissingQuantity(int size) {
        return 10 - size;
    }
}
