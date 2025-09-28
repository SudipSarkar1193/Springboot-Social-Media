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

    // Enumeration for Post Types
    public enum PostType {
        TEXT_IMAGE,
        VIDEO_SHORT
    }

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
    private List<String> imageUrls = new ArrayList<>();

    // New field for video URL
    @Column(name = "video_url",columnDefinition = "TEXT")
    private String videoUrl;

    // Field to distinguish post types
    @Enumerated(EnumType.STRING)
    @Column(name="post_type",nullable = false)
    private PostType postType = PostType.TEXT_IMAGE; // Default to existing type

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

    private long shareCount = 0; // DEFAULT val = 0

    // --- Relationships ---

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    @ToString.Exclude
    private User author;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<Like> likes = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_post_id")
    @ToString.Exclude
    private Post parentPost;

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