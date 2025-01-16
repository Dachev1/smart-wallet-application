package app.subscription.property;

import app.subscription.model.SubscriptionPeriod;
import app.subscription.model.SubscriptionStatus;
import app.subscription.model.SubscriptionType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

@Getter
@Setter
@ConfigurationProperties(prefix = "subscription")
public class SubscriptionsProperty {

    private SubscriptionType defaultType;
    private SubscriptionStatus defaultStatus;
    private SubscriptionPeriod defaultPeriod;
    private BigDecimal defaultPrice;
}
