package app.wallet.property;

import app.wallet.model.WalletStatus;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

@Getter
@Setter
@ConfigurationProperties(prefix = "wallet")
public class WalletsProperty {
    private WalletStatus defaultStatus;
    private BigDecimal defaultInitialBalance;
    private String defaultCurrency;
    private String systemName;
}
