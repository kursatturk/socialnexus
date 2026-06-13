package com.socialnexus.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.socialnexus.domain.Post;
import com.socialnexus.domain.User;
import com.socialnexus.graph.NetworkGraph;
import com.socialnexus.repository.UserRepository;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PostServiceTest {

    @Autowired
    private PostService postService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NetworkGraph networkGraph;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        userRepository.save(new User("alice", "a@test.io", "Alice", "bio", null));
        userRepository.save(new User("bob", "b@test.io", "Bob", "bio", null));
        networkGraph.addFollowing("alice", "bob");
    }

    @Test
    void createPost_doesNotAppearInNonFollowerFeed() {
        postService.createPost("alice", "Hello network");

        assertThat(postService.getOwnPosts("alice")).hasSize(1);
        assertThat(postService.getFeed("bob")).isEmpty();
        assertThat(postService.getFeed("alice")).isEmpty();
    }

    @Test
    void createPost_followerSeesFollowedPosts() {
        postService.createPost("bob", "Bob update");

        assertThat(postService.getFeed("alice")).hasSize(1);
        assertThat(postService.getFeed("alice").getFirst().content()).isEqualTo("Bob update");
        assertThat(postService.getFeed("alice").getFirst().author()).isEqualTo("bob");
    }

    @Test
    void getOwnPosts_returnsOnlyAuthorPosts() {
        postService.createPost("alice", "Alice post");
        postService.createPost("bob", "Bob post");

        assertThat(postService.getOwnPosts("alice")).hasSize(1);
        assertThat(postService.getOwnPosts("alice").getFirst().author()).isEqualTo("alice");
        assertThat(postService.getOwnPosts("bob")).hasSize(1);
    }

    @Test
    void createPost_withImageUrl() {
        Post post = postService.createPost("alice", "Photo caption", "https://example.com/img.jpg");

        assertThat(post.imageUrl()).isEqualTo("https://example.com/img.jpg");
    }

    @Test
    void followLifecycle_unfollowStopsFeed() {
        postService.createPost("bob", "Earlier post");
        assertThat(postService.getFeed("alice")).hasSize(1);

        networkGraph.removeFollowing("alice", "bob");
        assertThat(postService.getFeed("alice")).isEmpty();
    }
}
