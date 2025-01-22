package app.subscription.service;

import app.subscription.model.Subscription;
import app.subscription.property.SubscriptionsProperty;
import app.subscription.repository.SubscriptionRepository;
import app.user.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionsProperty subscriptionProperty;

    @Autowired
    public SubscriptionService(SubscriptionRepository subscriptionRepository, SubscriptionsProperty subscriptionProperty) {
        this.subscriptionRepository = subscriptionRepository;
        this.subscriptionProperty = subscriptionProperty;
    }

    public Subscription createDefaultSubscription(User user) {
        Subscription subscription = subscriptionRepository.save(initializeSubscription(user));

        log.info("Create default subscription with [id: %s] and type %s. For user %s with [id: %s]".formatted(subscription.getId(), subscription.getType(), user, user.getId()));

        return subscription;
    }

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

    public List<Subscription> getAllSubscriptionByUserId(UUID userId) {
        return subscriptionRepository.getAllByOwnerId(userId);
    }
}
