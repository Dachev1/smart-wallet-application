package app.user.service;

import app.subscription.model.Subscription;
import app.subscription.model.SubscriptionPeriod;
import app.subscription.model.SubscriptionStatus;
import app.subscription.model.SubscriptionType;
import app.subscription.repository.SubscriptionRepository;
import app.transaction.model.Transaction;
import app.transaction.model.TransactionStatus;
import app.transaction.model.TransactionType;
import app.transaction.repository.TransactionRepository;
import app.user.model.Country;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.repository.UserRepository;
import app.wallet.model.Wallet;
import app.wallet.model.WalletStatus;
import app.wallet.repository.WalletRepository;
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
    private final TransactionRepository transactionRepository;

    @Autowired
    public UserInit(UserRepository userRepository,
                    WalletRepository walletRepository,
                    SubscriptionRepository subscriptionRepository,
                    TransactionRepository transactionRepository) {
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.transactionRepository = transactionRepository;
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

        // Create Transactions
        Transaction transaction1 = Transaction.builder()
                .owner(user)
                .sender("user123")
                .receiver("merchant001")
                .amount(new BigDecimal("-50.00"))
                .balanceLeft(new BigDecimal("50.00"))
                .currency(Currency.getInstance("EUR"))
                .type(TransactionType.DEPOSIT)
                .status(TransactionStatus.SUCCEEDED)
                .description("Payment for Order #123")
                .createdOn(now.minusDays(2))
                .build();

        Transaction transaction2 = Transaction.builder()
                .owner(user)
                .sender("user123")
                .receiver("merchant002")
                .amount(new BigDecimal("-30.00"))
                .balanceLeft(new BigDecimal("50.00"))
                .currency(Currency.getInstance("EUR"))
                .type(TransactionType.DEPOSIT)
                .status(TransactionStatus.FAILED)
                .failureReason("Insufficient funds")
                .createdOn(now.minusDays(1))
                .build();

        Transaction transaction3 = Transaction.builder()
                .owner(user)
                .sender("merchant456")
                .receiver("user123")
                .amount(new BigDecimal("20.00"))
                .balanceLeft(new BigDecimal("70.00"))
                .currency(Currency.getInstance("EUR"))
                .type(TransactionType.WITHDRAWAL)
                .status(TransactionStatus.SUCCEEDED)
                .description("Refund for Order #456")
                .createdOn(now.minusHours(5))
                .build();

        transactionRepository.saveAndFlush(transaction1);
        transactionRepository.saveAndFlush(transaction2);
        transactionRepository.saveAndFlush(transaction3);

        log.info("User, wallet, subscription, and transactions initialized successfully");
    }
}
