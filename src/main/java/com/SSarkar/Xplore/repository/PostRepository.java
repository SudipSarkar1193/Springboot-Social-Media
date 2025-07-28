package com.SSarkar.Xplore.repository;

import com.SSarkar.Xplore.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

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
    @EntityGraph(attributePaths = {"author"})
    Page<Post> findAll(@NonNull Pageable pageable);
}