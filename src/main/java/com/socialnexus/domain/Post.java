package com.socialnexus.domain;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "posts")
@Getter
@Setter
@NoArgsConstructor
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 4000)
    private String content;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false, length = 64)
    private String authorUsername;

    @Column(length = 2048)
    private String imageUrl;

    public Post(String content, Instant createdAt, String authorUsername, String imageUrl) {
        this.content = content;
        this.createdAt = createdAt;
        this.authorUsername = authorUsername;
        this.imageUrl = (imageUrl != null && !imageUrl.isBlank()) ? imageUrl.trim() : null;
    }

    /** Thymeleaf / template compatibility with prior record accessors. */
    public String author() {
        return authorUsername;
    }

    public Instant timestamp() {
        return createdAt;
    }

    public String content() {
        return content;
    }

    public String imageUrl() {
        return imageUrl;
    }
}
