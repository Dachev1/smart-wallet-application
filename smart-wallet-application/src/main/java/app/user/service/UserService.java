package app.user.service;

import app.exeptions.DomainExceptions;
import app.subscription.model.Subscription;
import app.subscription.service.SubscriptionService;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.repository.UserRepository;
import app.wallet.model.Wallet;
import app.wallet.service.WalletService;
import app.web.dto.LoginRequest;
import app.web.dto.RegisterRequest;
import app.web.dto.UserEditRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SubscriptionService subscriptionService;
    private final WalletService walletService;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, SubscriptionService subscriptionService, WalletService walletService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.subscriptionService = subscriptionService;
        this.walletService = walletService;
    }

    public User login(LoginRequest loginRequest){

        Optional<User> userOptional = userRepository.findByUsername(loginRequest.getUsername());

        if (userOptional.isEmpty()){
            throw new DomainExceptions("Username or password is incorrect.");
        }

        User user = userOptional.get();

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new DomainExceptions("Username or password is incorrect.");
        }

        return user;
    }

    @Transactional
    public User register(RegisterRequest registerRequest){
        Optional<User> userOptional = userRepository.findByUsername(registerRequest.getUsername());
        if(userOptional.isPresent()){
            throw new DomainExceptions("Username [%s] already exist.".formatted(registerRequest.getUsername()));
        }

        User user = userRepository.save(initializeUser(registerRequest));

        Subscription defaultSubscription = subscriptionService.createDefaultSubscription(user);
        user.setSubscriptions(List.of(defaultSubscription));

        Wallet standardWallet = walletService.createNewWallet(user);
        user.setWallets(List.of(standardWallet));

        log.info("Successfully created new account for username [%s] and id [%s]".formatted(user.getUsername(), user.getId()));

         return user;
    }

    public void editUserDetails(UUID userId, UserEditRequest userEditRequest){

        User user = getById(userId);

        user.setFirstName(userEditRequest.getFirstName());
        user.setLastName(userEditRequest.getLastName());
        user.setEmail(userEditRequest.getEmail());
        user.setProfilePicture(userEditRequest.getProfilePicture());

        userRepository.save(user);

    }

     private User initializeUser(RegisterRequest registerRequest){
        return User.builder()
                .username(registerRequest.getUsername())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .role(UserRole.USER)
                .isActive(true)
                .country(registerRequest.getCountry())
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();
     }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getById(UUID id) {
        return userRepository.findById(id).orElseThrow(() -> new DomainExceptions("User with ID [%s] does not exist.".formatted(id)));

    }

    public void switchStatus(UUID id) {
        User user = getById(id);

        //if(user.isActive()){
        // user.setActive(false);
        // } else {
        // user.setActive(true);
        // } --->>>

        user.setActive(!user.isActive());
        userRepository.save(user);
    }
    public void switchRole(UUID id) {
        User user = getById(id);

        if (user.getRole() == UserRole.USER){
            user.setRole(UserRole.ADMIN);
        } else {
            user.setRole(UserRole.USER);
        }
        userRepository.save(user);
    }
}
