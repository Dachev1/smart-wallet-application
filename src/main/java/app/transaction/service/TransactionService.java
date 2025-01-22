package app.transaction.service;

import app.transaction.model.Transaction;
import app.transaction.model.TransactionStatus;
import app.transaction.model.TransactionType;
import app.transaction.repository.TransactionRepository;
import app.user.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public Transaction createTransaction(
            User owner,
            String sender,
            String receiver,
            BigDecimal amount,
            BigDecimal balanceLeft,
            Currency currency,
            TransactionType type,
            TransactionStatus status,
            String description,
            String failureReason
    ) {
        validateTransactionDetails(amount, balanceLeft);

        Transaction transaction = buildTransaction(
                owner,
                sender,
                receiver,
                amount,
                balanceLeft,
                currency,
                type,
                status,
                description,
                failureReason
        );

        Transaction savedTransaction = transactionRepository.save(transaction);
        log.info("Transaction created successfully with ID [{}]", savedTransaction.getId());
        return savedTransaction;
    }

    public List<Transaction> getUserTransactionsById(UUID userId) {
        return transactionRepository.getAllByOwnerId(userId);
    }

    public long getTotalTransactions() {
        return transactionRepository.count();
    }

    public BigDecimal getTotalTransactionAmount() {
        return transactionRepository.findAll().stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public long getTotalWithdrawals() {
        return transactionRepository.countByType(TransactionType.WITHDRAWAL);
    }

    public long getTotalDeposits() {
        return transactionRepository.countByType(TransactionType.DEPOSIT);
    }

    public long getSuccessfulTransactions() {
        return transactionRepository.countByStatus(TransactionStatus.SUCCEEDED);
    }

    public long getFailedTransactions() {
        return transactionRepository.countByStatus(TransactionStatus.FAILED);
    }

    // Private Helper Methods

    private void validateTransactionDetails(BigDecimal amount, BigDecimal balanceLeft) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transaction amount must be greater than zero");
        }

        if (balanceLeft == null) {
            throw new IllegalArgumentException("Balance left cannot be null");
        }
    }

    private Transaction buildTransaction(
            User owner,
            String sender,
            String receiver,
            BigDecimal amount,
            BigDecimal balanceLeft,
            Currency currency,
            TransactionType type,
            TransactionStatus status,
            String description,
            String failureReason
    ) {
        return Transaction.builder()
                .owner(owner)
                .sender(sender)
                .receiver(receiver)
                .amount(amount)
                .balanceLeft(balanceLeft)
                .currency(currency)
                .type(type)
                .status(status)
                .description(description)
                .failureReason(failureReason)
                .createdOn(LocalDateTime.now())
                .build();
    }
}
