package com.SSarkar.Xplore.repository;

import com.SSarkar.Xplore.entity.Post;
import com.SSarkar.Xplore.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    Optional<Post> findByUuid(UUID uuid);

    void deleteByUuid(UUID uuid);


    /**
     * Overriding findAll to solve the N+1 problem.
     *
     * @EntityGraph tells JPA to also fetch the related entities specified in 'attributePaths'.
     * In this case, we are fetching the 'author' of each post.
     * JPA will generate a single query with a JOIN to fetch Posts and their Authors together.
     */
    @Override
    @EntityGraph(attributePaths = {"author", "comments"})
    Page<Post> findAll(@NonNull Pageable pageable);


    /**
     * Finds a paginated list of all posts where the parentPost is null.
     * This is how we get only top-level posts, not comments.
     * We also use the @EntityGraph here to prevent the N+1 problem for the author.
     */
    @EntityGraph(attributePaths = {"author", "comments"})
    Page<Post> findAllByParentPostIsNull(Pageable pageable);

    @EntityGraph(attributePaths = {"author", "comments"})
    @Query("SELECT p FROM Post p WHERE p.author.uuid = :uuid")
    Page<Post> getPostsByAuthor(@Param("uuid") UUID uuid, Pageable pageable);

    @Query("SELECT p.uuid as postUuid, COUNT(l.id) as likeCount FROM Post p LEFT JOIN p.likes l WHERE p IN :posts GROUP BY p.uuid")
    List<Map<String, Object>> countLikesForPosts(@Param("posts") List<Post> posts);

    /**
     * Fetches a paginated list of Post UUIDs with a custom feed sorting:
     * <p>
     * Priority Order:
     * 1. Posts authored by the current user (always on top, newest first).
     * 2. Posts from followings that were created within the last 24 hours
     * (most recent first).
     * 3. All remaining posts (most recent first).
     * <p>
     * Note: Uses a CASE expression for sorting logic and delegates the 24-hour
     * window check to a parameter (`:yesterday`) for better portability across databases.
     */
    @Query("""

            SELECT p.uuid 
    FROM Post p 
    WHERE p.parentPost IS NULL AND p.postType = 'TEXT_IMAGE'
    ORDER BY 
        CASE 
            WHEN p.author IN (
                SELECT f.followee FROM Follow f WHERE f.follower = :currentUser
            )
            AND p.createdAt >= CURRENT_TIMESTAMP - 1 DAY
            THEN 0
            ELSE 1
        END,
        p.createdAt DESC
    """)
    Page<UUID> findFeedPostUuidsForUser(@Param("currentUser") UserDetails currentUser, Pageable pageable);


    /**
     * Fetches the full Post entities for a given list of UUIDs.
     * The @EntityGraph is applied here to efficiently load the author and comments.
     */
    @EntityGraph(attributePaths = {"author", "comments"})
    @Query("SELECT p FROM Post p WHERE p.uuid IN :uuids")
    List<Post> findByUuidIn(@Param("uuids") List<UUID> uuids);

    @Query("""
    SELECT p FROM Post p
    WHERE p.author IN (
        SELECT f.followee FROM Follow f WHERE f.follower.uuid = :currentUserUuid
    ) AND p.parentPost IS NULL 
    """)
    Page<Post> findPostsByFollowing(@Param("currentUserUuid") UUID currentUserUuid, Pageable pageable);

    @EntityGraph(attributePaths = {"author", "comments"})
    Page<Post> findAllByPostType(Post.PostType postType, Pageable pageable);

}