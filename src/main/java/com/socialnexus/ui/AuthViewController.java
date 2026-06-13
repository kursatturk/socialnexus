package com.socialnexus.ui;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.socialnexus.auth.AuthService;
import com.socialnexus.auth.SessionAuthService;
import com.socialnexus.exception.BadRequestException;
import com.socialnexus.exception.ConflictException;

import jakarta.servlet.http.HttpSession;

/**
 * Public authentication views (excluded from session interceptor).
 */
@Controller
public class AuthViewController {

    private final AuthService authService;
    private final SessionAuthService sessionAuthService;

    public AuthViewController(AuthService authService, SessionAuthService sessionAuthService) {
        this.authService = authService;
        this.sessionAuthService = sessionAuthService;
    }

    @GetMapping("/login")
    public String loginPage(HttpSession session) {
        if (sessionAuthService.isLoggedIn(session)) {
            return "redirect:/";
        }
        return "login";
    }

    @PostMapping("/login")
    public String login(
            @RequestParam String username,
            @RequestParam String password,
            HttpSession session,
            Model model) {
        if (!authService.authenticate(username, password)) {
            model.addAttribute("error", "Invalid username or password");
            model.addAttribute("username", username);
            return "login";
        }
        sessionAuthService.login(session, username.trim());
        return "redirect:/";
    }

    @GetMapping("/register")
    public String registerPage(HttpSession session) {
        if (sessionAuthService.isLoggedIn(session)) {
            return "redirect:/";
        }
        return "register";
    }

    @PostMapping("/register")
    public String register(
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String name,
            @RequestParam(required = false) String bio,
            @RequestParam(required = false) String skills,
            HttpSession session,
            Model model) {
        try {
            authService.register(username, email, password, name, bio, skills);
            sessionAuthService.login(session, username.trim());
            return "redirect:/";
        } catch (ConflictException | BadRequestException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("username", username);
            model.addAttribute("email", email);
            model.addAttribute("name", name);
            model.addAttribute("bio", bio);
            model.addAttribute("skills", skills);
            return "register";
        }
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        sessionAuthService.logout(session);
        return "redirect:/login";
    }
}
