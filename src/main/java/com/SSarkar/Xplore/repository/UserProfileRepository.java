package com.SSarkar.Xplore.repository;

import com.SSarkar.Xplore.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    // JpaRepository provides all the basic functionality we need (save, findById, etc.).
}