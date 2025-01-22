package app.subscription.service;

import app.subscription.model.Subscription;
import app.subscription.property.SubscriptionsProperty;
import app.subscription.repository.SubscriptionRepository;
import app.user.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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

    public SubscriptionService(SubscriptionRepository subscriptionRepository, SubscriptionsProperty subscriptionProperty) {
        this.subscriptionRepository = subscriptionRepository;
        this.subscriptionProperty = subscriptionProperty;
    }

    public Subscription createDefaultSubscription(User user) {
        validateUser(user);
        Subscription subscription = saveSubscription(initializeSubscription(user));
        logSubscriptionCreation(subscription, user);
        return subscription;
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
}
