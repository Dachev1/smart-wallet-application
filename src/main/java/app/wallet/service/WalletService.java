package app.wallet.service;

import app.user.model.User;
import app.wallet.model.Wallet;
import app.wallet.model.WalletStatus;
import app.wallet.repository.WalletRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;

@Slf4j
@Service
public class WalletService {
    private static final String INITIAL_BALANCE = "20";

    private final WalletRepository walletRepository;

    @Autowired
    public WalletService(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    public Wallet createNewWallet(User user) {
        Wallet wallet =  walletRepository.save(initializeWallet(user));

        log.info("Create new wallet with [id: %s] and balance %.2f. For user: %s with [id: %s]".formatted(wallet.getId(), wallet.getBalance(), user, user.getId()));

        return wallet;
    }

    private Wallet initializeWallet(User user) {
        LocalDateTime now = LocalDateTime.now();

        return Wallet.builder()
                .owner(user)
                .status(WalletStatus.ACTIVE)
                .balance(new BigDecimal(INITIAL_BALANCE)) // EUR 20
                .currency(Currency.getInstance("EUR"))
                .createdOn(now)
                .updatedOn(now)
                .build();
    }
}
