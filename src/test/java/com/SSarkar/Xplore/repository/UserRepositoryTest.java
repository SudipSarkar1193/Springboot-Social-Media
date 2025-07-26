package com.SSarkar.Xplore.repository;

import com.SSarkar.Xplore.entity.User;
import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class UserRepositoryTest {

    // 1. Get a logger instance for this specific class
    private static final Logger logger = LoggerFactory.getLogger(UserRepositoryTest.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password123");
        // We can add a log here to see the setup object
        testUser = userRepository.saveAndFlush(testUser);
        logger.info("Test setup complete. Saved initial user with ID: {}", testUser.getId());
    }

    @Test
    void whenSaveNewUser_thenIdIsGenerated() {
        // Arrange
        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setEmail("new@example.com");
        newUser.setPassword("newpassword");

        // Act
        User savedUser = userRepository.save(newUser);

        // 2. Use the logger instead of System.out
        logger.debug("Saved new user: {}", savedUser);

        // Assert
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getId()).isNotNull();
    }

    @Test
    void whenUpdateUser_thenUsernameIsChanged() {
        // Arrange
        String newUsername = "updatedUsername";
        Optional<User> userToUpdateOpt = userRepository.findById(testUser.getId());
        assertThat(userToUpdateOpt).isPresent();
        User userToUpdate = userToUpdateOpt.get();
        logger.debug("User before update: {}", userToUpdate);


        // Act
        userToUpdate.setUsername(newUsername);
        userRepository.save(userToUpdate);
        entityManager.flush();
        entityManager.clear();

        // Assert
        Optional<User> updatedUserOpt = userRepository.findById(testUser.getId());
        logger.debug("User after update lookup: {}", updatedUserOpt.orElse(null));

        assertThat(updatedUserOpt).isPresent();
        assertThat(updatedUserOpt.get().getUsername()).isEqualTo(newUsername);
    }

    @Test
    void whenFindByUsername_thenReturnUser() {
        // When
        Optional<User> foundUser = userRepository.findByUsername("testuser");

        logger.debug("Result of findByUsername('testuser'): {}", foundUser.orElse(null));

        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUsername()).isEqualTo(testUser.getUsername());
    }

    @Test
    void whenFindByInvalidUsername_thenReturnEmpty() {
        // When
        Optional<User> foundUser = userRepository.findByUsername("nonexistent");

        logger.debug("Result of findByUsername('nonexistent'): {}", foundUser.orElse(null));

        // Then
        assertThat(foundUser).isNotPresent();
    }

    // ... other tests remain the same ...
    @Test
    void whenExistsByEmail_thenReturnTrueForExistingEmail() {
        boolean exists = userRepository.existsByEmail("test@example.com");
        assertThat(exists).isTrue();
    }

    @Test
    void whenExistsByEmail_thenReturnFalseForNonExistingEmail() {
        boolean exists = userRepository.existsByEmail("wrong@example.com");
        assertThat(exists).isFalse();
    }
}