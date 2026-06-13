package com.socialnexus.ui;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.socialnexus.dto.UserResponse;
import com.socialnexus.graph.NetworkGraph;
import com.socialnexus.repository.UserRepository;
import com.socialnexus.ui.dto.UserCardView;

/**
 * In-memory user search — O(n) scan with early filtering over usernames and display names.
 */
@Service
public class SearchService {

    private final UserRepository userRepository;
    private final NetworkGraph networkGraph;

    public SearchService(UserRepository userRepository, NetworkGraph networkGraph) {
        this.userRepository = userRepository;
        this.networkGraph = networkGraph;
    }

    public List<UserCardView> search(String query, String activeUsername) {
        if (!StringUtils.hasText(query)) {
            return List.of();
        }
        String needle = query.trim().toLowerCase();
        return userRepository.findAllByOrderByUsernameAsc().stream()
                .map(UserResponse::from)
                .filter(user -> matches(needle, user))
                .map(user -> UserCardView.of(
                        user,
                        activeUsername,
                        networkGraph.hasConnection(activeUsername, user.username())))
                .toList();
    }

    private static boolean matches(String needle, UserResponse user) {
        return user.username().toLowerCase().contains(needle)
                || user.name().toLowerCase().contains(needle);
    }
}
