package app.wallet.service;

import app.exeptions.DomainExceptions;
import app.transaction.model.Transaction;
import app.transaction.model.TransactionStatus;
import app.transaction.model.TransactionType;
import app.transaction.service.TransactionService;
import app.user.model.User;
import app.wallet.model.Wallet;
import app.wallet.model.WalletStatus;
import app.wallet.repository.WalletRepository;
import app.web.dto.TransferRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class WalletService {


    private static final String SMART_WALLET_LTD = "SMART WALLET LTD";
    private final WalletRepository walletRepository;
    private final TransactionService transactionService;

    @Autowired
    public WalletService(WalletRepository walletRepository, TransactionService transactionService) {
        this.walletRepository = walletRepository;
        this.transactionService = transactionService;
    }

    public Wallet createNewWallet(User user) {

        Wallet wallet = walletRepository.save(initializeWallet(user));

        log.info("Successfully created new wallet with id [%s] and balance [%.2f].".formatted(wallet.getId(), wallet.getBalance()));

        return wallet;
    }

    @Transactional
    public Transaction topUp(UUID walletId, BigDecimal amount) {
        Wallet wallet = getWalletById(walletId);
        String transactionDescription = "Top up %.2f".formatted(amount.doubleValue());

        if (wallet.getStatus() == WalletStatus.INACTIVE) {
            return transactionService.createNewTransaction(
                    wallet.getOwner(),
                    SMART_WALLET_LTD,
                    walletId.toString(),
                    amount,
                    wallet.getBalance(),
                    wallet.getCurrency(),
                    TransactionType.DEPOSIT,
                    TransactionStatus.FAILED,
                    transactionDescription,
                    "Inactive wallet"
            );
        }

        wallet.setBalance(wallet.getBalance().add(amount));
        wallet.setUpdatedOn(LocalDateTime.now());

        walletRepository.save(wallet);
        return transactionService.createNewTransaction(
                wallet.getOwner(),
                SMART_WALLET_LTD,
                walletId.toString(),
                amount,
                wallet.getBalance(),
                wallet.getCurrency(),
                TransactionType.DEPOSIT,
                TransactionStatus.SUCCEEDED,
                transactionDescription,
                null
        );
    }

    public Transaction transferFunds(User sender, TransferRequest transferRequest) {

        Wallet senderWallet = getWalletById(transferRequest.getFromWalletId());

        Optional<Wallet> receiverWalletOptional = walletRepository.findAllByOwnerUsername(transferRequest.getToUserName())
                .stream()
                .filter(wallet -> wallet.getStatus() == WalletStatus.ACTIVE)
                .findFirst();

        String transferDescription = "Transfer from %s to %s, for %.2f".formatted(sender.getUsername(), transferRequest.getToUserName(), transferRequest.getAmount());

        if (receiverWalletOptional.isEmpty()){
            return transactionService.createNewTransaction(
                    sender,
                    senderWallet.getId().toString(),
                    transferRequest.getToUserName(),
                    transferRequest.getAmount(),
                    senderWallet.getBalance(),
                    senderWallet.getCurrency(),
                    TransactionType.WITHDRAWAL,
                    TransactionStatus.FAILED,
                    transferDescription,
                    "Invalid criteria for transfer"
                    );
        }

        //Money transfer Ivan -> Gosho | 20eur
        Transaction withdrawal = charge(sender, senderWallet.getId(), transferRequest.getAmount(), transferDescription);

        if (withdrawal.getStatus() == TransactionStatus.FAILED){
            return withdrawal;
        }
        Wallet receiverWallet = receiverWalletOptional.get();
        receiverWallet.setBalance(receiverWallet.getBalance().add(transferRequest.getAmount()));
        receiverWallet.setUpdatedOn(LocalDateTime.now());

        walletRepository.save(receiverWallet);
        transactionService.createNewTransaction(
                receiverWallet.getOwner(),
                senderWallet.getId().toString(),
                receiverWallet.getId().toString(),
                transferRequest.getAmount(),
                receiverWallet.getBalance(),
                receiverWallet.getCurrency(),
                TransactionType.DEPOSIT,
                TransactionStatus.SUCCEEDED,
                transferDescription,
                null
                );

        return withdrawal;
    }
    @Transactional
    public Transaction charge(User user, UUID walletId, BigDecimal amount, String description) {

        Wallet wallet = getWalletById(walletId);

        boolean isFailedTransaction = false;
        String failureReason = null;
        if (wallet.getStatus() == WalletStatus.INACTIVE) {
            failureReason = "Inactive wallet status";
            isFailedTransaction = true;
        }

        if (wallet.getBalance().compareTo(amount) < 0) {
            failureReason = "Wallet balance insufficient";
            isFailedTransaction = true;
        }

        if (isFailedTransaction) {
            return transactionService.createNewTransaction(user,
                    wallet.getId().toString(),
                    SMART_WALLET_LTD,
                    amount,
                    wallet.getBalance(),
                    wallet.getCurrency(),
                    TransactionType.WITHDRAWAL,
                    TransactionStatus.FAILED,
                    description,
                    failureReason
            );
        }


        wallet.setBalance(wallet.getBalance().subtract(amount));
        wallet.setUpdatedOn(LocalDateTime.now());

        walletRepository.save(wallet);


        return transactionService.createNewTransaction(
                user,
                wallet.getId().toString(),
                SMART_WALLET_LTD,
                amount,
                wallet.getBalance(),
                wallet.getCurrency(),
                TransactionType.WITHDRAWAL,
                TransactionStatus.SUCCEEDED,
                description,
                null
        );

    }

    private Wallet getWalletById(UUID walletId) {
        return walletRepository.findById(walletId)
                .orElseThrow(() -> new DomainExceptions("Wallet with id [%s] does not exist.".formatted(walletId)));
    }

    private Wallet initializeWallet(User user) {
        LocalDateTime now = LocalDateTime.now();

        return Wallet.builder()
                .owner(user)
                .status(WalletStatus.ACTIVE)
                .balance(new BigDecimal("20.00"))
                .currency(Currency.getInstance("EUR"))
                .createdOn(now)
                .updatedOn(now)
                .build();
    }
}
