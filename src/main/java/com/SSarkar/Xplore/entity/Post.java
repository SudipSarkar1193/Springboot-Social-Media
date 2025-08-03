package com.SSarkar.Xplore.entity;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
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

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "post_image_urls", joinColumns = @JoinColumn(name = "post_id"))
    @Column(name = "image_url", columnDefinition = "TEXT")
    private List<String> imageUrls = new ArrayList<>();;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

    // --- Relationships ---

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    @ToString.Exclude
    private User author;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<Like> likes = new ArrayList<>();

    /**
     * This is the parent post that this post is a comment on.
     * It's a Many-to-One relationship because many comments (posts) can belong to one parent post.
     * It can be null if the post is a top-level post, not a comment.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_post_id") // The foreign key column in the 'posts' table
    @ToString.Exclude
    private Post parentPost;

    /**
     * This is the list of comments on this post.
     * It's a One-to-Many relationship because one post can have many comments (which are also posts).
     * `mappedBy = "parentPost"` tells JPA that the `parentPost` field in the child `Post` entity owns this relationship.
     * `cascade = CascadeType.ALL` means if we delete a post, all its comments are also deleted.
     * `orphanRemoval = true` ensures that if a comment is removed from this list, it's also deleted from the database.
     */
    @OneToMany(mappedBy = "parentPost", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<Post> comments = new ArrayList<>();


    // --- Helper Methods for Bidirectional Consistency ---

    public void addComment(Post comment) {
        comments.add(comment);
        comment.setParentPost(this);
    }

    public void removeComment(Post comment) {
        comments.remove(comment);
        comment.setParentPost(null);
    }
}