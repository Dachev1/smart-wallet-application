package app.user.service;

import app.user.model.Country;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
public class UserInit implements CommandLineRunner {

    private final UserRepository userRepository;

    @Autowired
    public UserInit(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) throws Exception {

        LocalDateTime now = LocalDateTime.now();

        if (userRepository.count() != 0) {
            return;
        }

        User user = User.builder()
                .username("Pepa")
                .password("6_po_LAG")
                .role(UserRole.USER)
                .country(Country.FRANCE)
                .isActive(true)
                .createdOn(now)
                .updatedOn(now)
                .build();

        userRepository.save(user);
        log.info("User init succeed");
    }
}
