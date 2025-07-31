package com.SSarkar.Xplore.repository;

import com.SSarkar.Xplore.entity.Like;
import com.SSarkar.Xplore.entity.Post;
import com.SSarkar.Xplore.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {

    /**
     * Finds a Like entity by a specific user and post.
     * This is crucial for checking if a user has already liked a post,
     * or for finding the specific "like" to delete when a user unlikes a post.
     *
     * @param user The user who made the like.
     * @param post The post that was liked.
     * @return An Optional containing the Like if it exists, otherwise an empty Optional.
     */
    Optional<Like> findByUserAndPost(User user, Post post);

    /**
     * Finds all likes made by a specific user, paginated.
     * This is useful for displaying a user's liked posts in a paginated format.
     *
     * @param user The user whose likes are being queried.
     * @param pageable The pagination information.
     * @return A Page of Like entities made by the specified user.
     */
    Page<Like> findByUser(User user, Pageable pageable);
}