package com.SSarkar.Xplore.repository;

import com.SSarkar.Xplore.entity.User;
import com.SSarkar.Xplore.entity.UserProfile;


import org.junit.jupiter.api.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class UserProfileRepositoryTest {

    // 1. Get a logger instance for this class
    private static final Logger logger = LoggerFactory.getLogger(UserProfileRepositoryTest.class);

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void whenSaveUserWithProfile_thenProfileIsSaved() {
        // --- Arrange (Given) ---
        User user = new User();
        user.setUsername("profileuser");
        user.setEmail("profile@example.com");
        user.setPassword("password123");

        UserProfile profile = new UserProfile();
        profile.setBio("This is a test bio.");

        // Link them together
        user.setUserProfile(profile);

        //DEBUG: Log the User and UserProfile objects before saving
        logger.debug("Arranged User object before saving: {}", user);
        logger.debug("Arranged UserProfile object before saving: {}", profile);


        // --- Act (When) ---
        // Save the User. The UserProfile should be saved automatically due to the cascade.
        User savedUser = userRepository.saveAndFlush(user);
        logger.info("Saved User with cascaded Profile. User ID: {}, Profile ID: {}", savedUser.getId(), savedUser.getUserProfile().getId());


        // --- Assert (Then) ---
        // Retrieve the profile by the user's ID
        Optional<UserProfile> foundProfileOpt = userProfileRepository.findById(savedUser.getId());
        logger.debug("Looked up UserProfile from repository: {}", foundProfileOpt.orElse(null));


        // Check that the profile was saved
        assertThat(foundProfileOpt).isPresent();
        UserProfile foundProfile = foundProfileOpt.get();
        assertThat(foundProfile.getBio()).isEqualTo("This is a test bio.");

        // Check that the IDs match (verifying @MapsId)
        assertThat(foundProfile.getId()).isEqualTo(savedUser.getId());

        // Check that the back-reference from the profile to the user is correct
        assertThat(foundProfile.getUser()).isNotNull();
        assertThat(foundProfile.getUser().getUsername()).isEqualTo("profileuser");
        logger.info("Assertion successful: Profile correctly saved and linked to User.");
    }
}