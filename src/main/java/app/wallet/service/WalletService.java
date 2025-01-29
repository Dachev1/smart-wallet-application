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
import app.web.dto.TransferRequest;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.Map;
import java.util.Optional;
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
            return transactionService.createFailedTransaction(wallet, amount, "Inactive wallet", getUsernameByWallet(wallet));
        }

        updateWalletBalance(wallet, wallet.getBalance().add(amount));
        return transactionService.createSuccessfulTransaction(wallet, amount, "Top up", getUsernameByWallet(wallet));
    }

    @Transactional
    public Transaction charge(UUID walletId, BigDecimal amount, String description) {
        Wallet wallet = getWalletById(walletId);

        if (wallet == null) {
            return transactionService.createFailedTransaction(null, amount, "Wallet not found", getUsernameByWallet(wallet));
        }

        if (isWalletInactive(wallet)) {
            return transactionService.createFailedTransaction(wallet, amount, "Inactive wallet", getUsernameByWallet(wallet));
        }

        if (hasSufficientBalance(wallet, amount)) {
            return transactionService.createFailedTransaction(wallet, amount, "Insufficient balance", getUsernameByWallet(wallet));
        }

        updateWalletBalance(wallet, wallet.getBalance().subtract(amount));
        return transactionService.createSuccessfulTransaction(wallet, amount, description, getUsernameByWallet(wallet));
    }

    @Transactional
    public Transaction transferFunds(TransferRequest transferRequest, User userSender) {

        Wallet senderWallet = getWalletById(transferRequest.getFromWalletId());


        if (hasSufficientBalance(senderWallet, transferRequest.getAmount())) {
            return transactionService.createFailedTransaction(senderWallet, transferRequest.getAmount(), "Insufficient balance", getUsernameByWallet(senderWallet));
        }

        // Find recipient's wallet
        Optional<Wallet> receiverWalletOptional = walletRepository.findAllByOwnerUsername(transferRequest.getToUsername())
                .stream()
                .filter(wallet -> wallet.getStatus() == WalletStatus.ACTIVE)
                .findFirst();

        if (receiverWalletOptional.isEmpty()) {
            return transactionService.createFailedTransaction(senderWallet, transferRequest.getAmount(), "Recipient wallet is inactive or not found", "Doesn't exist");
        }

        // Charge sender's wallet
        Transaction senderTransaction = charge(senderWallet.getId(), transferRequest.getAmount(),
                "Transfer to " + transferRequest.getToUsername());

        if (senderTransaction.getStatus() == TransactionStatus.FAILED) {
            return senderTransaction;
        }

        Wallet recipientWallet = receiverWalletOptional.get();

        // Credit recipient's wallet
        updateWalletBalance(recipientWallet, recipientWallet.getBalance().add(transferRequest.getAmount()));

        // Create recipient's transaction record
        transactionService.createTransaction(
                recipientWallet.getOwner(),
                userSender.getUsername(),
                getUsernameByWallet(recipientWallet),
                transferRequest.getAmount(),
                recipientWallet.getBalance(),
                recipientWallet.getCurrency(),
                TransactionType.DEPOSIT,
                TransactionStatus.SUCCEEDED,
                "Received transfer from " + userSender.getUsername(),
                null
        );

        // Return the transaction for the sender
        return senderTransaction;
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

    private boolean isWalletInactive(Wallet wallet) {
        return wallet.getStatus() == WalletStatus.INACTIVE;
    }

    private boolean hasSufficientBalance(Wallet wallet, BigDecimal amount) {
        return wallet.getBalance().compareTo(amount) < 0;
    }

    private void updateWalletBalance(Wallet wallet, BigDecimal newBalance) {
        wallet.setBalance(newBalance);
        wallet.setUpdatedOn(LocalDateTime.now());
        walletRepository.save(wallet);
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

    private static String getUsernameByWallet(Wallet wallet) {
        return wallet.getOwner().getUsername();
    }
}
