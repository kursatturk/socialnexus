package com.socialnexus.dto;

import java.util.List;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterUserRequest(
        @NotBlank @Size(min = 3, max = 32) String username,
        @NotBlank @Email String email,
        @NotBlank String name,
        String bio,
        List<String> skills
) {
}
