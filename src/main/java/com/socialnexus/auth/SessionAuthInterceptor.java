package com.socialnexus.auth;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Redirects unauthenticated browser requests to {@code /login}.
 * REST {@code /api/**} routes are excluded in {@link com.socialnexus.config.WebMvcConfig}.
 */
@Component
public class SessionAuthInterceptor implements HandlerInterceptor {

    private final SessionAuthService sessionAuthService;

    public SessionAuthInterceptor(SessionAuthService sessionAuthService) {
        this.sessionAuthService = sessionAuthService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        HttpSession session = request.getSession(false);
        if (sessionAuthService.isLoggedIn(session)) {
            return true;
        }
        if (isHtmx(request)) {
            response.setHeader("HX-Redirect", "/login");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
        response.sendRedirect("/login");
        return false;
    }

    private static boolean isHtmx(HttpServletRequest request) {
        return "true".equalsIgnoreCase(request.getHeader("HX-Request"));
    }
}
