package com.SSarkar.Xplore.repository;

import com.SSarkar.Xplore.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
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

    Optional<Object> findByUuid(UUID uuid);

    @Query("""
    SELECT u FROM User u
    WHERE u.uuid <> :uuid
    AND u.uuid NOT IN (
        SELECT f.followee.uuid FROM Follow f
        WHERE f.follower.uuid = :uuid
    )
    ORDER BY u.createdAt DESC
    """)
    List<User> findTopUsersNotFollowedBy(@Param("uuid") UUID uuid, Pageable pageable);

    @Query("""
    SELECT u FROM User u
    WHERE u.uuid <> :uuid
    ORDER BY u.createdAt DESC
    """)
    Page<User> findAllUsersExceptForCurrentUser(@Param("uuid") UUID uuid, Pageable pageable);


    @Query("SELECT COUNT(f) FROM Follow f WHERE f.followee = :user")
    int countFollowers(@Param("user") User user);

    @Query("SELECT COUNT(f) FROM Follow f WHERE f.follower = :user")
    int countFollowing(@Param("user") User user);

    @Query("SELECT COUNT(p) FROM Post p WHERE p.author = :user")
    int countPosts(@Param("user") User user);
}