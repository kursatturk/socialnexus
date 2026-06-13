package com.socialnexus.auth;

import org.springframework.stereotype.Service;

import com.socialnexus.repository.UserRepository;

import jakarta.servlet.http.HttpSession;

@Service
public class SessionAuthService {

    private final UserRepository userRepository;

    public SessionAuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void login(HttpSession session, String username) {
        session.setAttribute(SessionConstants.CURRENT_USER, username);
    }

    public void logout(HttpSession session) {
        session.removeAttribute(SessionConstants.CURRENT_USER);
        session.invalidate();
    }

    public boolean isLoggedIn(HttpSession session) {
        if (session == null) {
            return false;
        }
        return getCurrentUsername(session) != null;
    }

    public String getCurrentUsername(HttpSession session) {
        if (session == null) {
            return null;
        }
        Object value = session.getAttribute(SessionConstants.CURRENT_USER);
        if (value instanceof String username && !username.isBlank()
                && userRepository.existsByUsername(username)) {
            return username;
        }
        return null;
    }

    /**
     * Returns the session username or throws if unauthenticated.
     */
    public String requireCurrentUser(HttpSession session) {
        String username = getCurrentUsername(session);
        if (username == null) {
            throw new UnauthorizedSessionException("Not authenticated");
        }
        return username;
    }
}
