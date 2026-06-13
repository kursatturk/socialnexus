package com.socialnexus.controller;

import java.util.Map;
import java.util.Set;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.socialnexus.service.NetworkService;

@RestController
@RequestMapping("/api/network")
public class NetworkController {

    private final NetworkService networkService;

    public NetworkController(NetworkService networkService) {
        this.networkService = networkService;
    }

    /**
     * {@code user1} follows {@code user2} (directed edge).
     */
    @PostMapping("/connect")
    public Map<String, Object> connect(
            @RequestParam String user1,
            @RequestParam String user2) {
        Set<String> connections = networkService.connect(user1, user2);
        return Map.of(
                "user1", user1,
                "user2", user2,
                "connections", connections
        );
    }
}
