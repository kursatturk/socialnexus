package com.socialnexus.auth;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.socialnexus.domain.Credential;
import com.socialnexus.dto.RegisterUserRequest;
import com.socialnexus.exception.BadRequestException;
import com.socialnexus.repository.CredentialRepository;
import com.socialnexus.repository.UserRepository;
import com.socialnexus.service.UserService;

@Service
public class AuthService {

    private final UserService userService;
    private final UserRepository userRepository;
    private final CredentialRepository credentialRepository;

    public AuthService(
            UserService userService,
            UserRepository userRepository,
            CredentialRepository credentialRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.credentialRepository = credentialRepository;
    }

    @Transactional
    public void register(String username, String email, String password, String name, String bio, String skillsCsv) {
        if (!StringUtils.hasText(password) || password.length() < 4) {
            throw new BadRequestException("Password must be at least 4 characters");
        }
        List<String> skills = parseSkills(skillsCsv);
        userService.register(new RegisterUserRequest(
                username.trim(),
                email.trim(),
                name.trim(),
                bio,
                skills));
        credentialRepository.save(new Credential(username.trim(), password));
    }

    public boolean authenticate(String username, String password) {
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            return false;
        }
        return credentialRepository.findByUsername(username.trim())
                .map(credential -> credential.getPassword().equals(password))
                .orElse(false);
    }

    @Transactional
    public void seedCredential(String username, String password) {
        if (userRepository.existsByUsername(username) && !credentialRepository.existsByUsername(username)) {
            credentialRepository.save(new Credential(username, password));
        }
    }

    private static List<String> parseSkills(String skillsCsv) {
        if (!StringUtils.hasText(skillsCsv)) {
            return List.of();
        }
        return Arrays.stream(skillsCsv.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .toList();
    }
}
