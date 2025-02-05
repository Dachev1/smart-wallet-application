package app.security;

import app.exception.DomainException;
import app.user.model.User;
import app.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Set;
import java.util.UUID;

@Component
public class AuthenticationInterceptor implements HandlerInterceptor {

    private static final Set<String> PUBLIC_ENDPOINTS = Set.of("/", "/login", "/register");
    private static final Set<String> ADMIN_ENDPOINTS  = Set.of("/users", "/reports");

    private final UserService userService;

    @Autowired
    public AuthenticationInterceptor(UserService userService) {
        this.userService = userService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        HttpSession session = request.getSession(false);
        String requestURI   = request.getRequestURI();

        boolean isAuthenticated  = isUserAuthenticated(session);
        boolean isPublicEndpoint = PUBLIC_ENDPOINTS.contains(requestURI);
        boolean isAdminEndpoint  = ADMIN_ENDPOINTS.contains(requestURI);

        // If not authenticated, allow only public endpoints
        if (!isAuthenticated) {
            if (!isPublicEndpoint) {
                redirectTo(response, request.getContextPath() + "/login");
                return false;
            }
            return true;
        }

        // Fetch user from DB
        UUID userId = (UUID) session.getAttribute("user_id");
        User user;
        try {
            user = userService.getById(userId);
        } catch (DomainException ex) {
            session.invalidate();
            redirectTo(response, request.getContextPath() + "/login");
            return false;
        }

        request.setAttribute("loggedUser", user);

        // If user is inactive, allow only public endpoints
        if (!user.isActive()) {
            if (!isPublicEndpoint) {
                session.invalidate();
                redirectTo(response, request.getContextPath() + "/login");
                return false;
            }
            return true;
        }

        // If admin endpoint, user must be admin
        if (isAdminEndpoint && !"ADMIN".equalsIgnoreCase(user.getRole().name())) {
            redirectTo(response, request.getContextPath() + "/home");
            return false;
        }

        // Otherwise, allow
        return true;
    }

    private boolean isUserAuthenticated(HttpSession session) {
        return session != null && session.getAttribute("user_id") != null;
    }

    private void redirectTo(HttpServletResponse response, String targetUrl) throws Exception {
        response.sendRedirect(targetUrl);
    }
}
