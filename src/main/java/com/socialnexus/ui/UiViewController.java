package com.socialnexus.ui;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.socialnexus.auth.SessionAuthService;
import com.socialnexus.exception.ResourceNotFoundException;

import jakarta.servlet.http.HttpSession;

/**
 * Authenticated Thymeleaf pages. Current user is always read from {@link HttpSession}.
 */
@Controller
public class UiViewController {

    private final UiService uiService;
    private final SessionAuthService sessionAuthService;

    public UiViewController(UiService uiService, SessionAuthService sessionAuthService) {
        this.uiService = uiService;
        this.sessionAuthService = sessionAuthService;
    }

    @GetMapping("/")
    public String feed(HttpSession session, Model model) {
        String activeUser = sessionAuthService.requireCurrentUser(session);
        UiModelAttributes.addCommon(model, uiService, activeUser, "feed");
        UiModelAttributes.addFeed(model, uiService, activeUser);
        return "index";
    }

    @GetMapping("/network")
    public String network(HttpSession session, Model model) {
        String activeUser = sessionAuthService.requireCurrentUser(session);
        UiModelAttributes.addCommon(model, uiService, activeUser, "network");
        UiModelAttributes.addNetwork(model, uiService, activeUser);
        return "network";
    }

    @GetMapping("/profile")
    public String profile(HttpSession session) {
        String activeUser = sessionAuthService.requireCurrentUser(session);
        return "redirect:/user/" + activeUser;
    }

    @GetMapping("/user/{username}")
    public String userProfile(
            @PathVariable String username,
            HttpSession session,
            Model model) {
        String activeUser = sessionAuthService.requireCurrentUser(session);
        try {
            var profileUser = uiService.profile(username);
            boolean isOwnProfile = activeUser.equals(username);
            UiModelAttributes.addCommon(model, uiService, activeUser, isOwnProfile ? "profile" : "user");
            model.addAttribute("profileUser", profileUser);
            model.addAttribute("isOwnProfile", isOwnProfile);
            var ownPosts = uiService.ownPosts(username);
            model.addAttribute("ownPosts", ownPosts);
            model.addAttribute("authorNames", uiService.authorDisplayNames(ownPosts));
            model.addAttribute("authorProfiles", uiService.authorProfiles(ownPosts));
            model.addAttribute("skillsCsv", String.join(", ", profileUser.skills()));
            model.addAttribute("profileConnectionCount", uiService.connectionCount(username));
            if (!isOwnProfile) {
                model.addAttribute("connectionCard", uiService.userCard(activeUser, username));
            }
            return "user-profile";
        } catch (ResourceNotFoundException ex) {
            return "redirect:/network";
        }
    }

    @GetMapping("/search")
    public String search(HttpSession session, Model model) {
        String activeUser = sessionAuthService.requireCurrentUser(session);
        UiModelAttributes.addCommon(model, uiService, activeUser, "search");
        model.addAttribute("query", "");
        model.addAttribute("results", java.util.List.of());
        return "search";
    }
}
