package com.SSarkar.Xplore.repository;

import com.SSarkar.Xplore.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
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
}