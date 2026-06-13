package com.socialnexus.ui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.socialnexus.domain.Post;
import com.socialnexus.domain.User;
import com.socialnexus.dto.UserResponse;
import com.socialnexus.exception.ResourceNotFoundException;
import com.socialnexus.graph.NetworkGraph;
import com.socialnexus.repository.UserRepository;
import com.socialnexus.service.PostService;
import com.socialnexus.service.UserService;
import com.socialnexus.ui.dto.UserCardView;

/**
 * Aggregates in-memory structures for Thymeleaf views and HTMX fragments.
 */
@Service
public class UiService {

    private final UserRepository userRepository;
    private final NetworkGraph networkGraph;
    private final PostService postService;
    private final UserService userService;

    public UiService(
            UserRepository userRepository,
            NetworkGraph networkGraph,
            PostService postService,
            UserService userService) {
        this.userRepository = userRepository;
        this.networkGraph = networkGraph;
        this.postService = postService;
        this.userService = userService;
    }

    public UserResponse profile(String username) {
        return UserResponse.from(userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username)));
    }

    public List<Post> feed(String username) {
        return postService.getFeed(username);
    }

    public List<Post> ownPosts(String username) {
        return postService.getOwnPosts(username);
    }

    public UserResponse updateProfile(String username, String bio, String skillsCsv) {
        return userService.updateProfile(username, bio, skillsCsv);
    }

    public UserResponse updateProfilePicture(String username, String profilePictureUrl) {
        return userService.updateProfilePicture(username, profilePictureUrl);
    }

    /**
     * Maps post author usernames to display names for feed cards. O(p) for p posts.
     */
    public Map<String, String> authorDisplayNames(List<Post> posts) {
        Map<String, String> names = new HashMap<>();
        for (Post post : posts) {
            names.computeIfAbsent(post.author(), author -> userRepository.findByUsername(author)
                    .map(User::getName)
                    .orElse(author));
        }
        return names;
    }

    public Map<String, UserResponse> authorProfiles(List<Post> posts) {
        Map<String, UserResponse> profiles = new HashMap<>();
        for (Post post : posts) {
            profiles.computeIfAbsent(post.author(), author -> profile(author));
        }
        return profiles;
    }

    public List<UserCardView> networkCards(String activeUsername) {
        Set<String> connections = networkGraph.getConnections(activeUsername);
        return userRepository.findAllByOrderByUsernameAsc().stream()
                .map(UserResponse::from)
                .map(user -> UserCardView.of(
                        user,
                        activeUsername,
                        connections.contains(user.username())))
                .toList();
    }

    /**
     * Users not connected to the active user (excluding self). O(n) scan over all users.
     */
    public List<UserCardView> connectionSuggestions(String activeUsername) {
        Set<String> connections = networkGraph.getConnections(activeUsername);
        return userRepository.findAllByOrderByUsernameAsc().stream()
                .map(UserResponse::from)
                .filter(user -> !user.username().equals(activeUsername))
                .filter(user -> !connections.contains(user.username()))
                .map(user -> UserCardView.of(user, activeUsername, false))
                .toList();
    }

    public UserCardView userCard(String activeUsername, String targetUsername) {
        UserResponse user = profile(targetUsername);
        boolean connected = networkGraph.hasConnection(activeUsername, targetUsername);
        return UserCardView.of(user, activeUsername, connected);
    }

    public int connectionCount(String username) {
        return networkGraph.getConnections(username).size();
    }

    /** Direct connections as profiles, sorted by username. O(d) for degree d. */
    public List<UserResponse> connectedProfiles(String activeUsername) {
        return networkGraph.getConnections(activeUsername).stream()
                .sorted()
                .map(this::profile)
                .toList();
    }
}
