package app.user.service;

import app.exception.DomainException;
import app.security.AuthenticationDetails;
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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class UserService implements UserDetailsService {

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
    public void register(RegisterRequest registerRequest) {
        validateUsername(registerRequest.getUsername());

        User newUser = createUser(registerRequest);

        assignDefaultResourcesToUser(newUser);

        log.info("Successfully registered user [{}] with ID [{}]", newUser.getUsername(), newUser.getId());
    }

    public void editUserDetails(UUID userId, UserEditRequest editRequest) {

        User user = getById(userId);

        user.setFirstName(editRequest.getFirstName());
        user.setLastName(editRequest.getLastName());
        user.setEmail(editRequest.getEmail());
        user.setProfilePicture(editRequest.getProfilePicture());

        user.setUpdatedOn(LocalDateTime.now());

        userRepository.save(user);

        log.info("User [{}] updated successfully", user.getId());
    }

    public User getById(UUID id) {
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

        walletService.initFirstWalletAfterRegister(user);

        subscriptionService.createDefaultSubscription(user);
    }

    public String getUsernameById(UUID userId) {
        return userRepository.findById(userId)
                .map(User::getUsername)
                .orElse(null);
    }

    public void toggleUserActiveStatus(UUID userId) {

        User user = getById(userId);
        user.setActive(!user.isActive());

        userRepository.save(user);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        User user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return new AuthenticationDetails(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.getRole(),
                user.isActive()
        );
    }
}
