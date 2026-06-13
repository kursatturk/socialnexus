package com.socialnexus.service;

import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.socialnexus.exception.ConflictException;
import com.socialnexus.exception.ResourceNotFoundException;
import com.socialnexus.graph.NetworkGraph;
import com.socialnexus.repository.UserRepository;

/**
 * Follow/unfollow management on top of the directed {@link NetworkGraph}.
 */
@Service
public class NetworkService {

    private final NetworkGraph networkGraph;
    private final UserRepository userRepository;
    private final PostService postService;

    public NetworkService(
            NetworkGraph networkGraph,
            UserRepository userRepository,
            PostService postService) {
        this.networkGraph = networkGraph;
        this.userRepository = userRepository;
        this.postService = postService;
    }

    @Transactional
    public Set<String> connect(String follower, String followed) {
        validateUserExists(follower);
        validateUserExists(followed);

        if (networkGraph.hasConnection(follower, followed)) {
            throw new ConflictException(follower + " already follows " + followed);
        }

        networkGraph.addFollowing(follower, followed);
        postService.backfillPostsFromFollowed(follower, followed);
        return networkGraph.getFollowing(follower);
    }

    @Transactional
    public Set<String> disconnect(String follower, String followed) {
        validateUserExists(follower);
        validateUserExists(followed);

        if (!networkGraph.removeFollowing(follower, followed)) {
            throw new ResourceNotFoundException(follower + " does not follow " + followed);
        }

        postService.purgeAuthorFromFeed(follower, followed);
        return networkGraph.getFollowing(follower);
    }

    public Set<String> getConnections(String username) {
        validateUserExists(username);
        return networkGraph.getFollowing(username);
    }

    private void validateUserExists(String username) {
        if (!userRepository.existsByUsername(username)) {
            throw new ResourceNotFoundException("User not found: " + username);
        }
    }
}
