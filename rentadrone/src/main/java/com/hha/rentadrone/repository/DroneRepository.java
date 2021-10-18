package com.hha.rentadrone.repository;

import com.hha.rentadrone.domain.Drone;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DroneRepository extends JpaRepository<Drone, Long> {
}
