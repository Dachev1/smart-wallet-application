package app.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;

public final class SessionUtils {

    // Private constructor to prevent instantiation.
    private SessionUtils() {}

    /**
     * Retrieves the user ID from the current HTTP session.
     *
     * @return the user ID as a UUID, or {@code null} if not found.
     */
    public static UUID getUserIdFromSession() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes == null) {
            return null;
        }

        HttpServletRequest request = attributes.getRequest();
        return (UUID) request.getSession().getAttribute("user_id");
    }
}
