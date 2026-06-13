package com.socialnexus.service;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.socialnexus.domain.Post;
import com.socialnexus.exception.BadRequestException;
import com.socialnexus.exception.ResourceNotFoundException;
import com.socialnexus.graph.NetworkGraph;
import com.socialnexus.repository.PostRepository;
import com.socialnexus.repository.UserRepository;

/**
 * Post creation and home-feed queries backed by JPA repositories.
 */
@Service
public class PostService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final NetworkGraph networkGraph;

    public PostService(
            UserRepository userRepository,
            PostRepository postRepository,
            NetworkGraph networkGraph) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.networkGraph = networkGraph;
    }

    @Transactional
    public Post createPost(String authorUsername, String content) {
        return createPost(authorUsername, content, null);
    }

    @Transactional
    public Post createPost(String authorUsername, String content, String imageUrl) {
        if (!StringUtils.hasText(content)) {
            throw new BadRequestException("Post content must not be blank");
        }

        if (!userRepository.existsByUsername(authorUsername)) {
            throw new ResourceNotFoundException("User not found: " + authorUsername);
        }

        String normalizedImageUrl = StringUtils.hasText(imageUrl) ? imageUrl.trim() : null;
        Post post = new Post(content.trim(), Instant.now(), authorUsername, normalizedImageUrl);
        return postRepository.save(post);
    }

    /**
     * Home timeline: posts from users the viewer follows, newest first.
     */
    @Transactional(readOnly = true)
    public List<Post> getFeed(String username) {
        requireUserExists(username);
        Set<String> following = networkGraph.getFollowing(username);
        
        // 2. Yeni küme oluşturup kullanıcının kendisini de listeye ekliyoruz
        Set<String> feedAuthors = new java.util.HashSet<>(following);
        feedAuthors.add(username);
        
        // 3. Eski "if (following.isEmpty())" kontrolünü sildik. 
        // Çünkü kullanıcı kimseyi takip etmese bile artık kendi postlarını görebilmeli.
        
        // 4. Veri tabanına "following" yerine "feedAuthors" kümesini gönderiyoruz
        return postRepository.findByAuthorUsernameInOrderByCreatedAtDesc(feedAuthors);
    }

    @Transactional(readOnly = true)
    public List<Post> getOwnPosts(String username) {
        requireUserExists(username);
        return postRepository.findByAuthorUsernameOrderByCreatedAtDesc(username);
    }

    /**
     * Home feed is computed from the database on each read; no cache backfill required.
     */
    public void backfillPostsFromFollowed(String followerUsername, String followedUsername) {
        requireUserExists(followerUsername);
        requireUserExists(followedUsername);
    }

    /**
     * Home feed is computed from the database on each read; no cache purge required.
     */
    public void purgeAuthorFromFeed(String followerUsername, String authorUsername) {
        requireUserExists(followerUsername);
        requireUserExists(authorUsername);
    }

    private void requireUserExists(String username) {
        if (!userRepository.existsByUsername(username)) {
            throw new ResourceNotFoundException("User not found: " + username);
        }
    }
}
