package app.user.service;

import app.exception.DomainException;
import app.subscription.service.SubscriptionService;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.property.UsersProperty;
import app.user.repository.UserRepository;
import app.wallet.service.WalletService;
import app.web.dto.LoginRequest;
import app.web.dto.RegisterRequest;
import app.web.dto.UserEditRequest;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class UserService {

    private final UserRepository userRepository;
    private final WalletService walletService;
    private final SubscriptionService subscriptionService;
    private final PasswordEncoder passwordEncoder;
    private final UsersProperty usersProperty;

    public UserService(UserRepository userRepository, WalletService walletService,
                       SubscriptionService subscriptionService, PasswordEncoder passwordEncoder,
                       UsersProperty usersProperty) {
        this.userRepository = userRepository;
        this.walletService = walletService;
        this.subscriptionService = subscriptionService;
        this.passwordEncoder = passwordEncoder;
        this.usersProperty = usersProperty;
    }

    @Transactional
    public User register(RegisterRequest registerRequest) {
        validateUsername(registerRequest.getUsername());
        User newUser = createUser(registerRequest);
        assignDefaultResourcesToUser(newUser);
        log.info("Successfully registered user [{}] with ID [{}]", newUser.getUsername(), newUser.getId());
        return newUser;
    }

    public User login(LoginRequest loginRequest) {
        User user = validateUserCredentials(loginRequest);
        log.info("User [{}] logged in successfully", user.getUsername());
        return user;
    }

    public void editUserDetails(UUID userId, UserEditRequest editRequest) {

        User user = getUserById(userId);

        user.setFirstName(editRequest.getFirstName());
        user.setLastName(editRequest.getLastName());
        user.setEmail(editRequest.getEmail());
        user.setProfilePicture(editRequest.getProfilePicture());

        user.setUpdatedOn(LocalDateTime.now());

        userRepository.save(user);

        log.info("User [{}] updated successfully", user.getId());
    }

    public User getUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new DomainException("User not found"));
    }

    public List<User> getAllUsers() {
        return userRepository.findAllByOrderByCreatedOnDesc();
    }

    public long getTotalUsers() {
        return userRepository.count();
    }

    public long getActiveUsers() {
        return userRepository.countByIsActive(true);
    }

    public long getInactiveUsers() {
        return userRepository.countByIsActive(false);
    }

    public long getAdmins() {
        return userRepository.countByRole(UserRole.ADMIN);
    }

    public long getNonAdmins() {
        return userRepository.countByRole(UserRole.USER);
    }

    // Private helper methods
    private void validateUsername(String username) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new DomainException("Username already exists");
        }
    }

    private User createUser(RegisterRequest registerRequest) {
        LocalDateTime now = LocalDateTime.now();
        return userRepository.save(
                User.builder()
                        .username(registerRequest.getUsername())
                        .password(passwordEncoder.encode(registerRequest.getPassword()))
                        .country(registerRequest.getCountry())
                        .role(usersProperty.getDefaultRole())
                        .isActive(usersProperty.isActiveByDefault())
                        .createdOn(now)
                        .updatedOn(now)
                        .build()
        );
    }

    private void assignDefaultResourcesToUser(User user) {
        walletService.createNewWallet(user);
        subscriptionService.createDefaultSubscription(user);
    }

    private User validateUserCredentials(LoginRequest loginRequest) {
        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new DomainException("Invalid username or password"));

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new DomainException("Invalid username or password");
        }

        if (!user.isActive()) {
            throw new DomainException("User is inactive");
        }

        return user;
    }
}
