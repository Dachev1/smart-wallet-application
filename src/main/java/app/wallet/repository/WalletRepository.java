package app.wallet.repository;

import app.user.model.User;
import app.wallet.model.Wallet;
import app.wallet.model.WalletStatus;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, UUID> {

    Optional<Wallet> findAllByOwnerUsername(String username);

    Collection<Wallet> findByOwner(User owner);
}
