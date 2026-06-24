package com.predicto.auth;

import com.predicto.common.enums.UserRole;
import com.predicto.wallet.Wallet;
import com.predicto.wallet.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminBootstrap implements CommandLineRunner {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.findByRole(UserRole.ADMIN).isEmpty()) {
            String username = System.getenv("ADMIN_BOOTSTRAP_USERNAME");
            String email = System.getenv("ADMIN_BOOTSTRAP_EMAIL");
            String password = System.getenv("ADMIN_BOOTSTRAP_PASSWORD");

            if (username == null || email == null || password == null) {
                log.warn("ADMIN_BOOTSTRAP_USERNAME, ADMIN_BOOTSTRAP_EMAIL, and ADMIN_BOOTSTRAP_PASSWORD must all be set to create an admin account. No admin account created.");
                return;
            }

            var admin = User.builder()
                    .username(username)
                    .displayName(username)
                    .email(email)
                    .passwordHash(passwordEncoder.encode(password))
                    .role(UserRole.ADMIN)
                    .badge("ADMIN")
                    .build();
            admin = userRepository.save(admin);
            walletRepository.save(Wallet.builder().user(admin).build());
            log.info("Created admin user via bootstrap ({})", username);
        }
    }
}
