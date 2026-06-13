package com.socialnexus.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "follow_edges",
        uniqueConstraints = @UniqueConstraint(columnNames = {"follower_username", "followed_username"}))
@Getter
@Setter
@NoArgsConstructor
public class FollowEdge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "follower_username", nullable = false, length = 64)
    private String followerUsername;

    @Column(name = "followed_username", nullable = false, length = 64)
    private String followedUsername;

    public FollowEdge(String followerUsername, String followedUsername) {
        this.followerUsername = followerUsername;
        this.followedUsername = followedUsername;
    }
}
