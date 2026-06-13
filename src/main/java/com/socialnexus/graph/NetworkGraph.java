package com.socialnexus.graph;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.socialnexus.domain.FollowEdge;
import com.socialnexus.repository.FollowEdgeRepository;
import com.socialnexus.repository.UserRepository;

/**
 * Directed follow graph backed by {@link FollowEdge} rows in the H2 database.
 */
@Component
public class NetworkGraph {

    private final FollowEdgeRepository followEdgeRepository;
    private final UserRepository userRepository;

    public NetworkGraph(FollowEdgeRepository followEdgeRepository, UserRepository userRepository) {
        this.followEdgeRepository = followEdgeRepository;
        this.userRepository = userRepository;
    }

    /**
     * No-op for JPA — users are vertices once persisted. Kept for registration flow compatibility.
     */
    public void ensureVertex(String username) {
        // Users exist as rows in the users table after registration.
    }

    /**
     * Establishes a directed follow edge: {@code follower} follows {@code followed}.
     */
    public boolean addFollowing(String follower, String followed) {
        if (follower.equals(followed)) {
            throw new IllegalArgumentException("Cannot follow yourself");
        }
        if (!userRepository.existsByUsername(follower) || !userRepository.existsByUsername(followed)) {
            throw new IllegalArgumentException("Follower or followed user does not exist");
        }
        if (followEdgeRepository.existsByFollowerUsernameAndFollowedUsername(follower, followed)) {
            return false;
        }
        followEdgeRepository.save(new FollowEdge(follower, followed));
        return true;
    }

    /**
     * Removes a directed follow edge.
     */
    public boolean removeFollowing(String follower, String followed) {
        if (!followEdgeRepository.existsByFollowerUsernameAndFollowedUsername(follower, followed)) {
            return false;
        }
        followEdgeRepository.deleteByFollowerUsernameAndFollowedUsername(follower, followed);
        return true;
    }

    public Set<String> getFollowing(String username) {
        return followEdgeRepository.findByFollowerUsername(username).stream()
                .map(FollowEdge::getFollowedUsername)
                .collect(Collectors.collectingAndThen(Collectors.toSet(), Collections::unmodifiableSet));
    }

    public Set<String> getConnections(String username) {
        return getFollowing(username);
    }

    public Set<String> getFollowers(String username) {
        return followEdgeRepository.findByFollowedUsername(username).stream()
                .map(FollowEdge::getFollowerUsername)
                .collect(Collectors.collectingAndThen(Collectors.toSet(), Collections::unmodifiableSet));
    }

    public boolean hasConnection(String follower, String followed) {
        return followEdgeRepository.existsByFollowerUsernameAndFollowedUsername(follower, followed);
    }
}
