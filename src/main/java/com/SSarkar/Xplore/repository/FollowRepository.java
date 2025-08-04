package com.SSarkar.Xplore.repository;

import com.SSarkar.Xplore.entity.Follow;
import com.SSarkar.Xplore.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface FollowRepository extends JpaRepository<Follow,Long> {

    Page<Follow> findByFollowee(User user, Pageable pageable);
    Page<Follow> findByFollower(User user, Pageable pageable);

    /**
     * Finds a Follow entity by follower ID and followee ID.
     * This is crucial for checking if a user is already following another user,
     * or for finding the specific "follow" to delete when a user unfollows another user.
     *
     * @param followerId The ID of the follower.
     * @param followeeId The ID of the followee.
     * @return An Optional containing the Follow if it exists, otherwise an empty Optional.
     */
    Optional<Follow> findByFollowerIdAndFolloweeId(Long followerId, Long followeeId);

    /**
     * Finds a Follow entity by the follower and followee User objects.
     * This is useful for checking if a user is following another user based on User objects.
     *
     * @param follower The User who is following.
     * @param followee The User who is being followed.
     * @return An Optional containing the Follow if it exists, otherwise an empty Optional.
     */
    Optional<Follow> findByFollowerAndFollowee(User follower, User followee);

    @Query("SELECT f.followee.uuid FROM Follow f WHERE f.follower = :currentUser AND f.followee IN :users")
    Set<UUID> findFollowingUuidsByCurrentUserAndUsers(@Param("currentUser") User currentUser, @Param("users") List<User> users);
}