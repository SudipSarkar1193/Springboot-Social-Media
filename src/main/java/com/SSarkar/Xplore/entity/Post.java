package com.SSarkar.Xplore.entity;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@ToString
@Entity
@Table(name = "posts")
@EqualsAndHashCode(of = "uuid")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false, unique = true)
    private UUID uuid = UUID.randomUUID();

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(columnDefinition = "TEXT")
    private List<String> imageUrls;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

    // --- Relationships ---

    /**
     * Interview Insight: @ManyToOne with FetchType.LAZY
     * ----------------------------------------------------
     * This is the OWNING side of the relationship. Many posts can belong to one user.
     *
     * FetchType.LAZY: This is a critical performance optimization. It means that when you
     * load a Post from the database, the associated User object WILL NOT be loaded at the
     * same time. It will only be fetched from the database when you explicitly access it
     * (e.g., by calling post.getAuthor()).
     *
     * Why is this important? Imagine fetching 100 posts for a news feed. With EAGER fetching,
     * you would execute 101 queries (1 for the posts, and 100 more for each user). With LAZY,
     * you only execute 1 query for the posts. This prevents the "N+1 select problem".
     *
     * JoinColumn: Specifies the foreign key column in the 'posts' table, which we'll name 'author_id'.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    @ToString.Exclude // Avoid recursion in toString()
    private User author;

    public void setAuthor(User author) {
        if (author == null) {
            throw new IllegalArgumentException("Author cannot be null");
        }
        this.author = author;

        author.getPosts().add(this);
    }

}