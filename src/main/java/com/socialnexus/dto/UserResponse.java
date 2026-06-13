package com.socialnexus.dto;

import java.util.List;

import com.socialnexus.domain.User;

public record UserResponse(
        String id,
        String username,
        String email,
        String name,
        String bio,
        List<String> skills,
        String profilePictureUrl
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                String.valueOf(user.getId()),
                user.getUsername(),
                user.getEmail(),
                user.getName(),
                user.getBio(),
                user.getSkills(),
                user.getProfilePictureUrl()
        );
    }
}
