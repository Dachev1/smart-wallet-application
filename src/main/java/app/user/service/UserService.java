package app.user.service;

import app.exception.DomainException;
import app.subscription.model.Subscription;
import app.subscription.service.SubscriptionService;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.repository.UserRepository;
import app.wallet.model.Wallet;
import app.wallet.service.WalletService;
import app.web.dto.LoginRequest;
import app.web.dto.RegisterRequest;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class UserService {

    private final UserRepository userRepository;
    private final WalletService walletService;
    private final SubscriptionService subscriptionService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, WalletService walletService, SubscriptionService subscriptionService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.walletService = walletService;
        this.subscriptionService = subscriptionService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User register(RegisterRequest registerRequest) {

        Optional<User> optionalUser = userRepository.findByUsername(registerRequest.getUsername());

        if(optionalUser.isPresent()) {
            throw new DomainException("Username already exists");
        }

        User savedUser = userRepository.save(initializeUser(registerRequest));

        Wallet standardWallet = walletService.createNewWallet(savedUser);
        savedUser.setWallets(List.of(standardWallet));

        Subscription defaultSubscription = subscriptionService.createDefaultSubscription(savedUser);
        savedUser.setSubscriptions(List.of(defaultSubscription));


        log.info("Successfully create new user account for username [%s] and id [%s]".formatted(savedUser.getUsername(), savedUser.getId()));
        return savedUser;
    }

    public User login(LoginRequest loginRequest) {
        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new DomainException("Invalid username or password"));



        return null;
    }

    private User initializeUser(RegisterRequest registerRequest) {
        LocalDateTime now = LocalDateTime.now();
        return User.builder()
                .username(registerRequest.getUsername())
                .password(registerRequest.getPassword())
                .country(registerRequest.getCountry())
                .role(UserRole.USER)
                .isActive(true)
                .createdOn(now)
                .updatedOn(now)
                .build();
    }
}
