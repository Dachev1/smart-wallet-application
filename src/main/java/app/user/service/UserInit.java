package app.user.service;

import app.user.model.Country;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.repository.UserRepository;
import app.wallet.model.Wallet;
import app.wallet.model.WalletStatus;
import app.wallet.repository.WalletRepository;
import app.subscription.model.Subscription;
import app.subscription.model.SubscriptionPeriod;
import app.subscription.model.SubscriptionStatus;
import app.subscription.model.SubscriptionType;
import app.subscription.repository.SubscriptionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;

@Slf4j
@Component
public class UserInit implements CommandLineRunner {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final SubscriptionRepository subscriptionRepository;

    @Autowired
    public UserInit(UserRepository userRepository,
                    WalletRepository walletRepository,
                    SubscriptionRepository subscriptionRepository) {
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
        this.subscriptionRepository = subscriptionRepository;
    }

    @Override
    public void run(String... args) throws Exception {

        LocalDateTime now = LocalDateTime.now();

        if (userRepository.count() != 0) {
            return;
        }

        // Create User
        User user = User.builder()
                .username("Pepa")
                .password("6_po_LAG")
                .role(UserRole.USER)
                .country(Country.FRANCE)
                .isActive(true)
                .createdOn(now)
                .updatedOn(now)
                .build();
        userRepository.save(user);

        // Create Wallet
        Wallet wallet = Wallet.builder()
                .owner(user)
                .balance(new BigDecimal("100.00"))
                .status(WalletStatus.ACTIVE)
                .currency(Currency.getInstance("EUR"))
                .createdOn(now)
                .updatedOn(now)
                .build();
        walletRepository.saveAndFlush(wallet);

        // Create Subscription
        Subscription subscription = Subscription.builder()
                .owner(user)
                .status(SubscriptionStatus.ACTIVE)
                .period(SubscriptionPeriod.MONTHLY)
                .type(SubscriptionType.ULTIMATE)
                .price(new BigDecimal("49.99"))
                .renewalAllowed(true)
                .createdOn(now)
                .completedOn(now.plusMonths(1))
                .build();
        subscriptionRepository.saveAndFlush(subscription);

        log.info("User, wallet, and subscription initialized successfully");
    }
}
