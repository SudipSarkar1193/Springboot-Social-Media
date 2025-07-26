package com.SSarkar.Xplore.repository;

import com.SSarkar.Xplore.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

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
}
