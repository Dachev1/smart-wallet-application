package app.security;

import app.user.model.User;
import app.user.model.UserRole;
import app.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Set;
import java.util.UUID;

@Component
public class SessionCheckInterceptor implements HandlerInterceptor {

    private final Set<String> UNAUTHENTICATED_ENDPOINTS = Set.of("/", "/login", "/register");
    private final Set<String> ADMIN_ENDPOINTS = Set.of("/users", "/reports");
    private final UserService userService;

    @Autowired
    public SessionCheckInterceptor(UserService userService) {
        this.userService = userService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String endpoint = request.getServletPath();
        if (UNAUTHENTICATED_ENDPOINTS.contains(endpoint)) {
            return true;
            //checking if the user is trying to access not authenticated end point -> one from the list above
        }

        HttpSession currentUserSession = request.getSession(false);
        if (currentUserSession == null) {
            response.sendRedirect("/login");
            return false;
        }

        UUID userId = (UUID) currentUserSession.getAttribute("user_id");
        User user = userService.getById(userId);

        if (!user.isActive()) {
            currentUserSession.invalidate();
            response.sendRedirect("/");
            return false;
        }
//        if (ADMIN_ENDPOINTS.contains(endpoint) && user.getRole() != UserRole.ADMIN) {
//           response.setStatus(HttpStatus.FORBIDDEN.value());
//           response.getWriter().write("Access denied, you don't have the necessary permission!");
//            return false;
//        }

        HandlerMethod handler1 = (HandlerMethod) handler;
        if (handler1.hasMethodAnnotation(RequireAdminRole.class) && user.getRole() != UserRole.ADMIN){
            response.setStatus(HttpStatus.FORBIDDEN.value());
           response.getWriter().write("Access denied, you don't have the necessary permission!");
            return false;
        }

        return true;
    }
}
