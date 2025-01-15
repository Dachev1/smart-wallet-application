package app.wallet.service;

import app.transaction.model.Transaction;
import app.transaction.model.TransactionStatus;
import app.transaction.model.TransactionType;
import app.transaction.service.TransactionService;
import app.user.model.User;
import app.wallet.model.Wallet;
import app.wallet.model.WalletStatus;
import app.wallet.repository.WalletRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.UUID;

@Slf4j
@Service
public class WalletService {
    private static final String INITIAL_BALANCE = "20";
    private static final String SMART_WALLET_LTD = "SMART_WALLET_LTD";
    private static final String WALLET_NOT_FOUNT = "Wallet not found";
    private static final String WALLET_INACTIVE= "Inactive wallet";
    private static final String INSUFFICIENT_BALANCE = "Insufficient balance";

    private final WalletRepository walletRepository;
    public final TransactionService transactionService;

    @Autowired
    public WalletService(WalletRepository walletRepository, TransactionService transactionService) {
        this.walletRepository = walletRepository;
        this.transactionService = transactionService;
    }

    public Wallet createNewWallet(User user) {
        Wallet wallet = walletRepository.save(initializeWallet(user));

        log.info("Create new wallet with [id: %s] and balance %.2f. For user: %s with [id: %s]".formatted(wallet.getId(), wallet.getBalance(), user, user.getId()));

        return wallet;
    }

    @Transactional
    public Transaction topUp(UUID walletId, BigDecimal amount) {

        Wallet wallet = getWalletById(walletId);
        String transactionDescription = "Top up %.2f".formatted(amount.doubleValue());

        if (wallet.getStatus() == WalletStatus.INACTIVE) {

            return transactionService.createNewTransaction(wallet.getOwner(),
                    SMART_WALLET_LTD,
                    walletId.toString(),
                    amount,
                    wallet.getBalance(),
                    wallet.getCurrency(),
                    TransactionType.DEPOSIT,
                    TransactionStatus.FAILED,
                    transactionDescription,
                    "Inactive wallet");
        }

        wallet.setBalance(wallet.getBalance().add(amount));
        wallet.setUpdatedOn(LocalDateTime.now());

        walletRepository.save(wallet);

        return transactionService.createNewTransaction(wallet.getOwner(),
                SMART_WALLET_LTD,
                walletId.toString(),
                amount,
                wallet.getBalance(),
                wallet.getCurrency(),
                TransactionType.DEPOSIT,
                TransactionStatus.SUCCEEDED,
                transactionDescription,
                null);
    }

    @Transactional
    public Transaction charge(UUID walletId, BigDecimal amount, String chargeDescription) {
        Wallet wallet = walletRepository.findById(walletId).orElse(null);
        TransactionStatus status = TransactionStatus.SUCCEEDED;
        String failReason = null;
        BigDecimal currentBalance = BigDecimal.ZERO;
        Currency currency = Currency.getInstance("EUR");
        User owner = null;

        if (wallet == null) {
            status = TransactionStatus.FAILED;
            failReason = WALLET_NOT_FOUNT;
        } else {
            owner = wallet.getOwner();
            currentBalance = wallet.getBalance();
            currency = wallet.getCurrency();

            if (wallet.getStatus() == WalletStatus.INACTIVE) {
                status = TransactionStatus.FAILED;
                failReason = WALLET_INACTIVE;
            } else if (currentBalance.compareTo(amount) < 0) {
                status = TransactionStatus.FAILED;
                failReason = INSUFFICIENT_BALANCE;
            } else {
                wallet.setBalance(currentBalance.subtract(amount));
                wallet.setUpdatedOn(LocalDateTime.now());
                walletRepository.save(wallet);
                currentBalance = wallet.getBalance();
            }
        }

        return transactionService.createNewTransaction(
                owner,
                walletId.toString(),
                SMART_WALLET_LTD,
                amount,
                currentBalance,
                currency,
                TransactionType.WITHDRAWAL,
                status,
                chargeDescription,
                failReason
        );
    }

    private Wallet getWalletById(UUID walletId) {
        return walletRepository.findById(walletId).orElse(null);
    }

    private Wallet initializeWallet(User user) {
        LocalDateTime now = LocalDateTime.now();

        return Wallet.builder()
                .owner(user)
                .status(WalletStatus.ACTIVE)
                .balance(new BigDecimal(INITIAL_BALANCE)) // EUR 20
                .currency(Currency.getInstance("EUR"))
                .createdOn(now)
                .updatedOn(now)
                .build();
    }
}
