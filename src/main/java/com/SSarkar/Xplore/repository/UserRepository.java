package com.SSarkar.Xplore.repository;

import com.SSarkar.Xplore.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // This interface extends JpaRepository, which provides basic CRUD operations.
    // We can define custom query methods here, and Spring Data JPA will automatically implement them.


    // This method will return an Optional<User> because the username might not exist in the database, and we want to handle that case gracefully.
    // If the username exists, it will return an Optional containing the User.
    // If it does not exist, it will return an empty Optional.
    Optional<User> findByUsername(String username);


    Optional<User> findByEmail(String email);

    // We'll need these for validation during user registration to ensure
    // the chosen username and email are not already taken.
    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    // This method will return an Optional<User> that matches either the username or email.
    // If a user with the given username or email exists, it will return an Optional containing the User; otherwise, it will return an empty Optional.

    /**
     * This is the correct approach for a login system.
     * It finds a user where EITHER the username OR the email matches the single
     * input string provided from the login form.
     */
    @Query("SELECT u FROM User u WHERE u.username = :login OR u.email = :login")
    Optional<User> findByUsernameOrEmail(@Param("login") String login);

    Optional<Object> findByUuid(UUID userToFollowUuid);
}
