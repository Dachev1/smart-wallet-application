package app.validation;

import app.user.service.UserService;
import app.util.SessionUtils;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class SelfTransferNotAllowedFieldValidator implements ConstraintValidator<SelfTransferNotAllowed, String> {

    private final UserService userService;

    @Autowired
    public SelfTransferNotAllowedFieldValidator(UserService userService) {
        this.userService = userService;
    }

    @Override
    public boolean isValid(String toUsername, ConstraintValidatorContext context) {
        // Let other validations (like @NotBlank) handle null/empty values
        if (toUsername == null || toUsername.isBlank()) {
            return true;
        }

        // Retrieve the current user id from the session using a helper (see SessionUtils below)
        UUID userId = SessionUtils.getUserIdFromSession();
        if (userId == null) {
            // Alternatively, you may choose to pass validation if no user is logged in,
            // or mark it as invalid.
            return false;
        }

        // Retrieve the logged-in user's username
        String loggedInUsername = userService.getUsernameById(userId);
        if (loggedInUsername == null) {
            return false;
        }

        // Return false if the provided toUsername is the same as the logged-in username (ignoring case)
        return !toUsername.equalsIgnoreCase(loggedInUsername);
    }
}
