package com.socialnexus.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.socialnexus.domain.FollowEdge;

public interface FollowEdgeRepository extends JpaRepository<FollowEdge, Long> {

    boolean existsByFollowerUsernameAndFollowedUsername(String followerUsername, String followedUsername);

    void deleteByFollowerUsernameAndFollowedUsername(String followerUsername, String followedUsername);

    List<FollowEdge> findByFollowerUsername(String followerUsername);

    List<FollowEdge> findByFollowedUsername(String followedUsername);
}
