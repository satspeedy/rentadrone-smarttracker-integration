package com.hha.rentadrone.service;

import com.hha.rentadrone.domain.User;
import com.hha.rentadrone.repository.UserRepository;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Save a user.
     *
     * @param user the entity to save.
     * @return the persisted entity.
     */
    @SneakyThrows
    public User save(User user) {
        log.info("Request to save User : {}", StringifyHelper.toJson(user));
        return userRepository.save(user);
    }

    /**
     * Partially update a user.
     *
     * @param user the entity to update partially.
     * @return the persisted entity.
     */
    @SneakyThrows
    public Optional<User> partialUpdate(User user) {
        log.info("Request to partially update User : {}", StringifyHelper.toJson(user));

        return userRepository
                .findById(user.getId())
                .map(
                        existingUser -> {
                            if (user.getUserName() != null) {
                                existingUser.setUserName(user.getUserName());
                            }
                            if (user.getName() != null) {
                                existingUser.setName(user.getName());
                            }
                            return existingUser;
                        }
                )
                .map(userRepository::save);
    }

    /**
     * Get all the users.
     *
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public List<User> findAll() {
        log.info("Request to get all Users");
        return userRepository.findAll();
    }

    /**
     * Get one user by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<User> findOne(Long id) {
        log.info("Request to get User : {}", id);
        return userRepository.findById(id);
    }

    /**
     * Get one user by id.
     *
     * @param userName the userName of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<User> findOne(String userName) {
        log.info("Request to get User : {}", userName);
        return userRepository.findOneByUserName(userName);
    }

    /**
     * Delete the user by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        log.info("Request to delete User : {}", id);
        userRepository.deleteById(id);
    }
}
