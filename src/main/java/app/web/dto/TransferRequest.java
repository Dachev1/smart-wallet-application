package app.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@WalletOwner
@SelfTransferNotAllowed

@Data
public class TransferRequest {

    @NotNull(message = "Sender wallet ID is required")
    private UUID fromWalletId;

    @NotBlank(message = "Recipient username is required")
    private String toUsername;

    @NotNull(message = "Transfer amount is required")
    @Min(value = 1, message = "Transfer amount must be at least 1")
    private BigDecimal amount;
}
