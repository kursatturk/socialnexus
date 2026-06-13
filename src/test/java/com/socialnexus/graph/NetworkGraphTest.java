package com.socialnexus.graph;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.socialnexus.domain.User;
import com.socialnexus.repository.FollowEdgeRepository;
import com.socialnexus.repository.UserRepository;

@DataJpaTest
@Import(NetworkGraph.class)
class NetworkGraphTest {

    @Autowired
    private NetworkGraph graph;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FollowEdgeRepository followEdgeRepository;

    @BeforeEach
    void seedUsers() {
        followEdgeRepository.deleteAll();
        userRepository.deleteAll();
        userRepository.save(new User("a", "a@test.io", "A", null, null));
        userRepository.save(new User("b", "b@test.io", "B", null, null));
    }

    @Test
    void addFollowing_isDirected() {
        graph.addFollowing("a", "b");

        assertThat(graph.getFollowing("a")).containsExactly("b");
        assertThat(graph.getFollowing("b")).isEmpty();
        assertThat(graph.getFollowers("b")).containsExactly("a");
        assertThat(graph.getFollowers("a")).isEmpty();
    }

    @Test
    void removeFollowing_removesOnlyOneDirection() {
        graph.addFollowing("a", "b");

        assertThat(graph.removeFollowing("a", "b")).isTrue();
        assertThat(graph.getFollowing("a")).isEmpty();
        assertThat(followEdgeRepository.count()).isZero();
    }

    @Test
    void addFollowing_rejectsSelfLoop() {
        assertThatThrownBy(() -> graph.addFollowing("a", "a"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
