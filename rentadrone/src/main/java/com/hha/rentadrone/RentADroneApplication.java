package com.hha.rentadrone;

import com.github.javafaker.Faker;
import com.hha.rentadrone.domain.Drone;
import com.hha.rentadrone.domain.User;
import com.hha.rentadrone.domain.enumeration.DroneStatus;
import com.hha.rentadrone.domain.enumeration.OperationStatus;
import com.hha.rentadrone.repository.DroneRepository;
import com.hha.rentadrone.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.annotation.EnableKafka;

import java.util.List;
import java.util.Optional;

@Slf4j
@SpringBootApplication
@EnableKafka
public class RentADroneApplication {

	@Autowired
	private DroneRepository droneRepository;

	@Autowired
	private UserRepository userRepository;

	private final Faker faker = new Faker();

	public static void main(String[] args) {
		SpringApplication.run(RentADroneApplication.class, args);
	}

	@EventListener(ApplicationReadyEvent.class)
	public void runAfterStartup() {
		addDronesToDB();
		addAdminUsersToDB();
	}

	private void addAdminUsersToDB() {
		Optional<User> superAdmin = userRepository.findOneByUserName("superadmin");
		if (superAdmin.isEmpty()) {
			userRepository.save(User.builder().userName("superadmin").name("Susan Super Admin").build());
		}
		Optional<User> userAdmin = userRepository.findOneByUserName("admin");
		if (userAdmin.isEmpty()) {
			userRepository.save(User.builder().userName("admin").name("Adam Admin").build());
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
		String nickName = faker.superhero().prefix() + faker.name().firstName() + faker.address().buildingNumber();
		String model = faker.aviation().aircraft();

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
