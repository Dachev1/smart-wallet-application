package app.subscription.service;

import app.subscription.model.Subscription;
import app.subscription.model.SubscriptionPeriod;
import app.subscription.model.SubscriptionStatus;
import app.subscription.model.SubscriptionType;
import app.subscription.property.SubscriptionsProperty;
import app.subscription.repository.SubscriptionRepository;
import app.transaction.model.Transaction;
import app.transaction.model.TransactionStatus;
import app.user.model.User;
import app.wallet.service.WalletService;
import app.web.dto.UpgradeRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionsProperty subscriptionProperty;
    private final WalletService walletService;

    @Autowired
    public SubscriptionService(SubscriptionRepository subscriptionRepository, SubscriptionsProperty subscriptionProperty, WalletService walletService) {
        this.subscriptionRepository = subscriptionRepository;
        this.subscriptionProperty = subscriptionProperty;
        this.walletService = walletService;
    }

    public void createDefaultSubscription(User user) {
        validateUser(user);
        Subscription subscription = saveSubscription(initializeSubscription(user));
        logSubscriptionCreation(subscription, user);
    }

    public Transaction createSubscription(User user, SubscriptionType type, UpgradeRequest upgradeRequest) {

        validateUser(user);

        BigDecimal cost = determinePrice(type, upgradeRequest.getPeriod());

        String description = String.format("Subscription upgrade: %s - %s", type, upgradeRequest.getPeriod());
        Transaction transaction = walletService.charge(upgradeRequest.getWalletId(), cost, description);


        if (transaction.getStatus() == TransactionStatus.FAILED) {
            return transaction;
        }

        completeLastActiveSubscription(user);

        subscriptionRepository.save(buildNewSubscription(user, type, upgradeRequest.getPeriod(), cost));

        return transaction;

    }

    public List<Subscription> getAllSubscriptionsByUserId(UUID userId) {
        return subscriptionRepository.getAllByOwnerId(userId);
    }

    public Map<String, Long> getSubscriptionSummary() {
        return subscriptionRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        subscription -> subscription.getType().toString(),
                        Collectors.counting()
                ));
    }

    public long getTotalSubscriptions() {
        return subscriptionRepository.count();
    }

    // Private Helper Methods

    private Subscription initializeSubscription(User user) {
        LocalDateTime now = LocalDateTime.now();
        return Subscription.builder()
                .owner(user)
                .type(subscriptionProperty.getDefaultType())
                .status(subscriptionProperty.getDefaultStatus())
                .period(subscriptionProperty.getDefaultPeriod())
                .price(subscriptionProperty.getDefaultPrice())
                .renewalAllowed(true)
                .createdOn(now)
                .completedOn(now.plusMonths(1))
                .build();
    }

    private Subscription saveSubscription(Subscription subscription) {
        return subscriptionRepository.save(subscription);
    }

    private void logSubscriptionCreation(Subscription subscription, User user) {
        log.info("Created subscription [ID: {}] of type [{}] for user [ID: {}, Username: {}]",
                subscription.getId(), subscription.getType(), user.getId(), user.getUsername());
    }

    private void validateUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null when creating a subscription");
        }
    }

    private BigDecimal determinePrice(SubscriptionType type, SubscriptionPeriod period) {

        switch (type) {
            case PREMIUM -> {
                return (period == SubscriptionPeriod.MONTHLY)
                        ? BigDecimal.valueOf(19.99)
                        : BigDecimal.valueOf(199.99);
            }

            case ULTIMATE -> {
                return (period == SubscriptionPeriod.MONTHLY)
                        ? BigDecimal.valueOf(49.99)
                        : BigDecimal.valueOf(499.99);
            }

            default -> {
                return BigDecimal.ZERO;
            }
        }
    }

    private LocalDateTime getCompletionDate(LocalDateTime start, SubscriptionPeriod period) {
        return (period == SubscriptionPeriod.MONTHLY)
                ? start.plusMonths(1)
                : start.plusYears(1);
    }

    private void completeLastActiveSubscription(User user) {
        List<Subscription> subs = user.getSubscriptions();

        if (!subs.isEmpty()) {
            Subscription newest = subs.get(0);

            if (newest.getStatus() == SubscriptionStatus.ACTIVE) {
                newest.setStatus(SubscriptionStatus.COMPLETED);
                newest.setCompletedOn(LocalDateTime.now());

                subscriptionRepository.save(newest);
            }
        }
    }

    private Subscription buildNewSubscription(User user, SubscriptionType type, SubscriptionPeriod period, BigDecimal cost) {
        LocalDateTime now = LocalDateTime.now();

        return Subscription.builder()
                .owner(user)
                .type(type)
                .status(SubscriptionStatus.ACTIVE)
                .period(period)
                .price(cost)
                .renewalAllowed(true)
                .createdOn(now)
                .completedOn(getCompletionDate(now, period))
                .build();
    }

}
