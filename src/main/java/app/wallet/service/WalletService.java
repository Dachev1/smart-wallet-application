package app.wallet.service;

import app.transaction.model.Transaction;
import app.transaction.model.TransactionStatus;
import app.transaction.model.TransactionType;
import app.transaction.service.TransactionService;
import app.user.model.User;
import app.wallet.model.Wallet;
import app.wallet.model.WalletStatus;
import app.wallet.property.WalletsProperty;
import app.wallet.repository.WalletRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class WalletService {

    private final WalletRepository walletRepository;
    private final TransactionService transactionService;
    private final WalletsProperty walletsProperty;

    public WalletService(WalletRepository walletRepository, TransactionService transactionService, WalletsProperty walletsProperty) {
        this.walletRepository = walletRepository;
        this.transactionService = transactionService;
        this.walletsProperty = walletsProperty;
    }

    public Wallet createNewWallet(User user) {
        Wallet wallet = initializeWallet(user);
        walletRepository.save(wallet);
        log.info("Created wallet for user [{}] with ID [{}]", user.getUsername(), wallet.getId());
        return wallet;
    }

    @Transactional
    public Transaction topUp(UUID walletId, BigDecimal amount) {
        Wallet wallet = getWalletById(walletId);

        if (isWalletInactive(wallet)) {
            return createFailedTransaction(wallet, amount, "Inactive wallet");
        }

        updateWalletBalance(wallet, wallet.getBalance().add(amount));
        return createSuccessfulTransaction(wallet, amount, "Top up");
    }

    @Transactional
    public Transaction charge(UUID walletId, BigDecimal amount, String description) {
        Wallet wallet = getWalletById(walletId);

        if (wallet == null) {
            return createFailedTransaction(null, amount, "Wallet not found");
        }

        if (isWalletInactive(wallet)) {
            return createFailedTransaction(wallet, amount, "Inactive wallet");
        }

        if (!hasSufficientBalance(wallet, amount)) {
            return createFailedTransaction(wallet, amount, "Insufficient balance");
        }

        updateWalletBalance(wallet, wallet.getBalance().subtract(amount));
        return createSuccessfulTransaction(wallet, amount, description);
    }

    private boolean isWalletInactive(Wallet wallet) {
        return wallet.getStatus() == WalletStatus.INACTIVE;
    }

    private boolean hasSufficientBalance(Wallet wallet, BigDecimal amount) {
        return wallet.getBalance().compareTo(amount) >= 0;
    }

    private void updateWalletBalance(Wallet wallet, BigDecimal newBalance) {
        wallet.setBalance(newBalance);
        wallet.setUpdatedOn(LocalDateTime.now());
        walletRepository.save(wallet);
    }

    private Transaction createSuccessfulTransaction(Wallet wallet, BigDecimal amount, String description) {
        return transactionService.createTransaction(
                wallet.getOwner(),
                wallet.getId().toString(),
                walletsProperty.getSystemName(),
                amount,
                wallet.getBalance(),
                wallet.getCurrency(),
                TransactionType.DEPOSIT,
                TransactionStatus.SUCCEEDED,
                description,
                null
        );
    }

    private Transaction createFailedTransaction(Wallet wallet, BigDecimal amount, String reason) {
        return transactionService.createTransaction(
                wallet != null ? wallet.getOwner() : null,
                wallet != null ? wallet.getId().toString() : "N/A",
                walletsProperty.getSystemName(),
                amount,
                wallet != null ? wallet.getBalance() : BigDecimal.ZERO,
                wallet != null ? wallet.getCurrency() : Currency.getInstance("USD"),
                TransactionType.DEPOSIT,
                TransactionStatus.FAILED,
                "Transaction failed",
                reason
        );
    }

    private Wallet initializeWallet(User user) {
        LocalDateTime now = LocalDateTime.now();
        return Wallet.builder()
                .owner(user)
                .status(walletsProperty.getDefaultStatus())
                .balance(walletsProperty.getDefaultInitialBalance())
                .currency(Currency.getInstance(walletsProperty.getDefaultCurrency()))
                .createdOn(now)
                .updatedOn(now)
                .build();
    }

    private Wallet getWalletById(UUID walletId) {
        return walletRepository.findById(walletId).orElseThrow(() -> new IllegalArgumentException("Wallet not found"));
    }

    public long getTotalWallets() {
        return walletRepository.count();
    }

    public BigDecimal getTotalWalletAmount() {
        return walletRepository.findAll().stream()
                .map(Wallet::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Map<String, Long> getWalletDistribution() {
        return walletRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        wallet -> wallet.getOwner().getWallets().size() + " Wallets",
                        Collectors.counting()
                ));
    }
}
