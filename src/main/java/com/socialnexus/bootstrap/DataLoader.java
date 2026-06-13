package com.socialnexus.bootstrap;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.socialnexus.domain.Post;
import com.socialnexus.auth.AuthService;
import com.socialnexus.dto.RegisterUserRequest;
import com.socialnexus.repository.UserRepository;
import com.socialnexus.service.NetworkService;
import com.socialnexus.service.PostService;
import com.socialnexus.service.UserService;

/**
 * Seeds sample users, connections, and posts; prints timelines to verify LIFO ordering.
 */
@Component
@Profile("!test")
public class DataLoader implements CommandLineRunner {

    private static final String DEMO_PASSWORD = "password";

    private final UserService userService;
    private final NetworkService networkService;
    private final PostService postService;
    private final AuthService authService;
    private final UserRepository userRepository;

    public DataLoader(
            UserService userService,
            NetworkService networkService,
            PostService postService,
            AuthService authService,
            UserRepository userRepository) {
        this.userService = userService;
        this.networkService = networkService;
        this.postService = postService;
        this.authService = authService;
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }
        seedUsers();
        seedConnections();
        seedPosts();
        printTimelines();
    }

    private void seedUsers() {
        userService.register(new RegisterUserRequest(
                "alice", "alice@socialnexus.io", "Alice Chen",
                "Backend engineer passionate about distributed systems.",
                List.of("Java", "Spring", "Kafka")));
        userService.register(new RegisterUserRequest(
                "bob", "bob@socialnexus.io", "Bob Martinez",
                "Full-stack developer building developer tools.",
                List.of("TypeScript", "React", "Node")));
        userService.register(new RegisterUserRequest(
                "carol", "carol@socialnexus.io", "Carol Nguyen",
                "Data scientist focused on graph analytics.",
                List.of("Python", "Neo4j", "ML")));
        userService.register(new RegisterUserRequest(
                "dave", "dave@socialnexus.io", "Dave Okonkwo",
                "Cloud architect designing resilient platforms.",
                List.of("AWS", "Kubernetes", "Terraform")));
        authService.seedCredential("alice", DEMO_PASSWORD);
        authService.seedCredential("bob", DEMO_PASSWORD);
        authService.seedCredential("carol", DEMO_PASSWORD);
        authService.seedCredential("dave", DEMO_PASSWORD);
        System.out.println("Demo logins: alice/bob/carol/dave — password: " + DEMO_PASSWORD);
    }

    private void seedConnections() {
        networkService.connect("alice", "bob");
        networkService.connect("alice", "carol");
        networkService.connect("bob", "dave");
        networkService.connect("carol", "dave");
    }

    private void seedPosts() {
        postService.createPost("alice", "Shipped a new Spring Boot microservice today.");
        postService.createPost("bob", "Open-sourced our internal CLI for graph migrations.");
        postService.createPost("carol", "Published notes on adjacency-list vs matrix tradeoffs.");
        postService.createPost("alice", "Second post from Alice — should appear above the first on neighbors' feeds.");
        postService.createPost("dave", "Kicked off a zero-downtime deployment runbook.");
        postService.createPost(
                "carol",
                "Graph visualization snapshot from this week's analysis sprint.",
                "https://images.unsplash.com/photo-1551288049-bebda4e38f71?w=800&q=80");
    }

    private void printTimelines() {
        System.out.println();
        System.out.println("=== SocialNexus — LIFO feed verification (newest first) ===");
        for (String username : List.of("alice", "bob", "carol", "dave")) {
            List<Post> feed = postService.getFeed(username);
            System.out.println();
            System.out.println("Timeline for @" + username + " (" + feed.size() + " posts):");
            if (feed.isEmpty()) {
                System.out.println("  (empty — home feed shows posts from users you follow)");
            } else {
                int rank = 1;
                for (Post post : feed) {
                    System.out.printf("  %d. [%s] %s — \"%s\"%n",
                            rank++, post.timestamp(), post.author(), post.content());
                }
            }
        }
        System.out.println();
    }
}
