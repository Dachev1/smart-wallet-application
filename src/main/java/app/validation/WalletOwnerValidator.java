package app.validation;

import app.user.service.UserService;
import app.util.SessionUtils;
import app.wallet.service.WalletService;
import app.web.dto.TransferRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class WalletOwnerValidator implements ConstraintValidator<WalletOwner, TransferRequest> {

    private final WalletService walletService;
    private final UserService userService;

    @Autowired
    public WalletOwnerValidator(WalletService walletService, UserService userService) {
        this.walletService = walletService;
        this.userService = userService;
    }

    @Override
    public boolean isValid(TransferRequest transferRequest, ConstraintValidatorContext context) {
        if (transferRequest == null || transferRequest.getFromWalletId() == null) {
            return true;
        }

        UUID userId = SessionUtils.getUserIdFromSession();
        if (userId == null) {
            return false;
        }

        return walletService.getWalletsByUserId(userService.getById(userId))
                .stream()
                .anyMatch(wallet -> wallet.getId().equals(transferRequest.getFromWalletId()));
    }
}
