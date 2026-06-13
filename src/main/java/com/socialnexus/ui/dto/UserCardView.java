package com.socialnexus.ui.dto;

import java.util.List;

import com.socialnexus.dto.UserResponse;

/**
 * View model for network grid cards and connection suggestions.
 */
public record UserCardView(
        String username,
        String name,
        String bio,
        List<String> skills,
        String profilePictureUrl,
        boolean connected,
        boolean self
) {
    public static UserCardView of(UserResponse user, String activeUsername, boolean connected) {
        return new UserCardView(
                user.username(),
                user.name(),
                user.bio() != null ? user.bio() : "",
                user.skills(),
                user.profilePictureUrl(),
                connected,
                user.username().equals(activeUsername)
        );
    }

    public String initials() {
        if (name == null || name.isBlank()) {
            return username.substring(0, 1).toUpperCase();
        }
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, 1).toUpperCase();
        }
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }
}
