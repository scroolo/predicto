package com.predicto.auth;

import com.predicto.auth.security.JwtUtil;
import com.predicto.common.enums.UserRole;
import com.predicto.wallet.Wallet;
import com.predicto.wallet.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
public class DiscordAuthService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final JwtUtil jwtUtil;

    private static final String CHARS = "abcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom RND = new SecureRandom();

    @Transactional
    public DiscordLoginResult loginOrRegister(DiscordProfile profile) {
        var byDiscord = userRepository.findByDiscordId(profile.id());
        if (byDiscord.isPresent()) {
            var user = byDiscord.get();
            String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole().name());
            return new DiscordLoginResult(token, user.getId(), user.getUsername(), user.getRole().name());
        }

        if (profile.email() != null) {
            var byEmail = userRepository.findByEmail(profile.email());
            if (byEmail.isPresent()) {
                var user = byEmail.get();
                user.setDiscordId(profile.id());
                if (user.getAvatarUrl() == null && profile.avatar() != null) {
                    user.setAvatarUrl("https://cdn.discordapp.com/avatars/" + profile.id() + "/" + profile.avatar() + ".png");
                }
                userRepository.save(user);
                String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole().name());
                return new DiscordLoginResult(token, user.getId(), user.getUsername(), user.getRole().name());
            }
        }

        String baseUsername = profile.username().toLowerCase();
        String username = baseUsername;
        while (userRepository.findByUsername(username).isPresent()) {
            username = baseUsername + "_" + randomSuffix();
        }

        String avatarUrl = profile.avatar() != null
                ? "https://cdn.discordapp.com/avatars/" + profile.id() + "/" + profile.avatar() + ".png"
                : null;

        var user = User.builder()
                .username(username)
                .displayName(profile.username())
                .email(profile.email())
                .discordId(profile.id())
                .avatarUrl(avatarUrl)
                .passwordHash(null)
                .role(UserRole.USER)
                .build();
        user = userRepository.save(user);

        walletRepository.save(Wallet.builder().user(user).lolElo(100).cs2Elo(100).build());

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole().name());
        return new DiscordLoginResult(token, user.getId(), user.getUsername(), user.getRole().name());
    }

    private static String randomSuffix() {
        var sb = new StringBuilder(3);
        for (int i = 0; i < 3; i++) {
            sb.append(CHARS.charAt(RND.nextInt(CHARS.length())));
        }
        return sb.toString();
    }

    public record DiscordLoginResult(String token, java.util.UUID userId, String username, String role) {}
}
