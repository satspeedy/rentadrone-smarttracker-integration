package com.hha.rentadrone.repository;

import com.hha.rentadrone.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findOneByUserName(String userName);
}
