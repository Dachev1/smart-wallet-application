package app.web.dto;

import app.subscription.model.SubscriptionPeriod;
import app.subscription.model.SubscriptionStatus;
import lombok.Data;

import java.util.UUID;

@Data
public class UpgradeRequest {

    private SubscriptionPeriod period;

    private UUID walletId;
}
