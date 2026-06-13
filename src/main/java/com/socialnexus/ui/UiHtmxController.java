package com.socialnexus.ui;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.socialnexus.auth.SessionAuthService;
import com.socialnexus.domain.Post;
import com.socialnexus.dto.UserResponse;
import com.socialnexus.service.NetworkService;
import com.socialnexus.service.PostService;

import jakarta.servlet.http.HttpSession;

/**
 * Session-aware HTMX endpoints used by the Thymeleaf UI ({@code hx-post}, {@code hx-get}).
 */
@Controller
public class UiHtmxController {

    private final SessionAuthService sessionAuthService;
    private final PostService postService;
    private final NetworkService networkService;
    private final UiService uiService;
    private final SearchService searchService;

    public UiHtmxController(
            SessionAuthService sessionAuthService,
            PostService postService,
            NetworkService networkService,
            UiService uiService,
            SearchService searchService) {
        this.sessionAuthService = sessionAuthService;
        this.postService = postService;
        this.networkService = networkService;
        this.uiService = uiService;
        this.searchService = searchService;
    }

    @PostMapping("/posts/create")
    public String createPost(
            @RequestParam String content,
            @RequestParam(value = "imageUrl", required = false) String imageUrl,
            HttpSession session,
            Model model) {
        String currentUser = currentUser(session);
        Post post = postService.createPost(currentUser, content, imageUrl);
        model.addAttribute("post", post);
        model.addAttribute("authorNames", uiService.authorDisplayNames(java.util.List.of(post)));
        model.addAttribute("authorProfiles", uiService.authorProfiles(java.util.List.of(post)));
        return "fragments/feed-post :: card";
    }

    @PostMapping("/network/connect")
    public String connect(
            @RequestParam String target,
            @RequestParam(value = "refresh", required = false) String refresh,
            @RequestParam(value = "q", required = false, defaultValue = "") String query,
            HttpSession session,
            Model model) {
        return networkMutation(session, model, target, refresh, query, true);
    }

    @DeleteMapping("/network/disconnect")
    public String disconnect(
            @RequestParam String target,
            @RequestParam(value = "refresh", required = false) String refresh,
            @RequestParam(value = "q", required = false, defaultValue = "") String query,
            HttpSession session,
            Model model) {
        return networkMutation(session, model, target, refresh, query, false);
    }

    @PostMapping("/user/profile")
    public String updateProfile(
            @RequestParam String bio,
            @RequestParam(value = "skills", required = false, defaultValue = "") String skills,
            HttpSession session,
            Model model) {
        String currentUser = currentUser(session);
        UserResponse updated = uiService.updateProfile(currentUser, bio, skills);
        model.addAttribute("profileUser", updated);
        model.addAttribute("isOwnProfile", true);
        model.addAttribute("skillsCsv", String.join(", ", updated.skills()));
        return "fragments/profile-bio :: section";
    }

    @PostMapping("/user/profile-picture")
    public String updateProfilePicture(
            @RequestParam(value = "profilePictureUrl", required = false) String profilePictureUrl,
            HttpSession session,
            Model model) {
        String currentUser = currentUser(session);
        UserResponse updated = uiService.updateProfilePicture(currentUser, profilePictureUrl);
        model.addAttribute("profileUser", updated);
        model.addAttribute("isOwnProfile", true);
        model.addAttribute("activeUser", currentUser);
        model.addAttribute("connectionCount", uiService.connectionCount(currentUser));
        return "fragments/profile-avatar-settings :: settings";
    }

    @GetMapping("/search/results")
    public String searchResults(
            @RequestParam(value = "q", required = false, defaultValue = "") String query,
            HttpSession session,
            Model model) {
        String currentUser = currentUser(session);
        model.addAttribute("activeUser", currentUser);
        model.addAttribute("query", query);
        model.addAttribute("results", searchService.search(query, currentUser));
        return "fragments/search-results :: results";
    }

    private String networkMutation(
            HttpSession session,
            Model model,
            String target,
            String refresh,
            String query,
            boolean connect) {
        String currentUser = currentUser(session);
        if (connect) {
            networkService.connect(currentUser, target);
        } else {
            networkService.disconnect(currentUser, target);
        }
        model.addAttribute("card", uiService.userCard(currentUser, target));
        model.addAttribute("activeUser", currentUser);
        model.addAttribute("connectionCount", uiService.connectionCount(currentUser));
        model.addAttribute("activeProfile", uiService.profile(currentUser));

        if ("sidebar".equals(refresh)) {
            model.addAttribute("suggestions", uiService.connectionSuggestions(currentUser));
            return "fragments/sidebar-oob :: updates";
        }
        if ("search".equals(refresh)) {
            model.addAttribute("results", searchService.search(query, currentUser));
            model.addAttribute("query", query);
            return "fragments/search-results :: results";
        }
        if ("grid".equals(refresh)) {
            model.addAttribute("suggestions", uiService.connectionSuggestions(currentUser));
            return connect
                    ? "fragments/network-actions :: connectResponse"
                    : "fragments/network-actions :: disconnectResponse";
        }
        if ("profile".equals(refresh)) {
            model.addAttribute("connectionCard", uiService.userCard(currentUser, target));
            return "fragments/profile-connection :: control";
        }
        return "fragments/connect-control :: slot";
    }

    private String currentUser(HttpSession session) {
        return sessionAuthService.requireCurrentUser(session);
    }
}
