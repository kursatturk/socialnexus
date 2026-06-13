package com.socialnexus.ui;

import java.util.List;
import java.util.Map;

import org.springframework.ui.Model;

import com.socialnexus.domain.Post;

/**
 * Shared model attributes for UI controllers.
 */
public final class UiModelAttributes {

    private UiModelAttributes() {
    }

    public static void addCommon(
            Model model,
            UiService uiService,
            String activeUser,
            String activePage) {
        model.addAttribute("activeUser", activeUser);
        model.addAttribute("activePage", activePage);
        model.addAttribute("pageTitle", pageTitle(activePage));
        model.addAttribute("activeProfile", uiService.profile(activeUser));
        model.addAttribute("suggestions", uiService.connectionSuggestions(activeUser));
        model.addAttribute("connectionCount", uiService.connectionCount(activeUser));
    }

    public static void addFeed(
            Model model,
            UiService uiService,
            String activeUser) {
        List<Post> posts = uiService.feed(activeUser);
        model.addAttribute("posts", posts);
        model.addAttribute("authorNames", uiService.authorDisplayNames(posts));
        model.addAttribute("authorProfiles", uiService.authorProfiles(posts));
    }

    public static void addNetwork(Model model, UiService uiService, String activeUser) {
        model.addAttribute("networkCards", uiService.networkCards(activeUser));
    }

    private static String pageTitle(String activePage) {
        return switch (activePage) {
            case "network" -> "Network";
            case "profile", "user" -> "Profile";
            case "search" -> "Search";
            default -> "Feed";
        };
    }
}
