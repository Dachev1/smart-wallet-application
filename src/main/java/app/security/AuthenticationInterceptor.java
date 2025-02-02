package app.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Set;

@Component
public class AuthenticationInterceptor implements HandlerInterceptor {

    private static final Set<String> PUBLIC_ENDPOINTS = Set.of("/", "/login", "/register");

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        final HttpSession session = request.getSession(false);
        final String requestURI = request.getRequestURI();
        final boolean isAuthenticated = isUserAuthenticated(session);
        final boolean isPublicEndpoint = PUBLIC_ENDPOINTS.contains(requestURI);

        if (isAuthenticated && isPublicEndpoint) {
            // Authenticated users should not access public endpoints
            redirectTo(response, request.getContextPath() + "/home");
            return true;
        }

        if (!isAuthenticated && !isPublicEndpoint) {
            // Unauthenticated users can only access public endpoints
            redirectTo(response, request.getContextPath() + "/login");
            return false;
        }

        // Allow the request to proceed
        return true;
    }

    private boolean isUserAuthenticated(HttpSession session) {
        return session != null && session.getAttribute("user_id") != null;
    }

    private void redirectTo(HttpServletResponse response, String targetUrl) throws Exception {
        response.sendRedirect(targetUrl);
    }
}
