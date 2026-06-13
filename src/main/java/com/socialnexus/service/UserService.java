package com.socialnexus.service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.socialnexus.domain.User;
import com.socialnexus.dto.RegisterUserRequest;
import com.socialnexus.dto.UserResponse;
import com.socialnexus.exception.ConflictException;
import com.socialnexus.exception.ResourceNotFoundException;
import com.socialnexus.graph.NetworkGraph;
import com.socialnexus.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final NetworkGraph networkGraph;

    public UserService(UserRepository userRepository, NetworkGraph networkGraph) {
        this.userRepository = userRepository;
        this.networkGraph = networkGraph;
    }

    @Transactional
    public UserResponse register(RegisterUserRequest request) {
        String username = request.username().trim();
        String email = request.email().trim().toLowerCase();

        if (userRepository.existsByUsername(username)) {
            throw new ConflictException("Username already exists: " + username);
        }
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("Email already registered: " + email);
        }

        User user = new User(
                username,
                email,
                request.name().trim(),
                request.bio(),
                request.skills());
        userRepository.save(user);
        networkGraph.ensureVertex(user.getUsername());
        return UserResponse.from(user);
    }

    public UserResponse getProfile(String username) {
        return UserResponse.from(requireUser(username));
    }

    public User requireUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }

    @Transactional
    public UserResponse updateProfile(String username, String bio, String skillsCsv) {
        User user = requireUser(username);
        user.setBio(bio != null ? bio.trim() : "");
        user.setSkills(parseSkills(skillsCsv));
        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse updateProfilePicture(String username, String profilePictureUrl) {
        User user = requireUser(username);
        user.setProfilePictureUrl(StringUtils.hasText(profilePictureUrl) ? profilePictureUrl.trim() : null);
        return UserResponse.from(user);
    }

    private static List<String> parseSkills(String skillsCsv) {
        if (!StringUtils.hasText(skillsCsv)) {
            return List.of();
        }
        return Arrays.stream(skillsCsv.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .collect(Collectors.toList());
    }
}
