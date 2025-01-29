package app.transaction.repository;

import app.transaction.model.Transaction;
import app.transaction.model.TransactionStatus;
import app.transaction.model.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    List<Transaction> getAllByOwnerIdOrderByCreatedOnDesc(UUID userId);

    long countByType(TransactionType type);

    long countByStatus(TransactionStatus status);
}
