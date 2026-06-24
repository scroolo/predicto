package com.predicto.admin;

import com.predicto.auth.User;
import com.predicto.auth.UserRepository;
import com.predicto.common.enums.Game;
import com.predicto.rank.RankService;
import com.predicto.season.RankTier;
import com.predicto.wallet.Wallet;
import com.predicto.wallet.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/leaderboard")
@RequiredArgsConstructor
public class AdminLeaderboardController {

    private final WalletRepository walletRepository;
    private final UserRepository userRepository;
    private final RankService rankService;

    @GetMapping("/lol")
    public ResponseEntity<List<Map<String, Object>>> getLolLeaderboard() {
        return getLeaderboard(Game.LOL);
    }

    @GetMapping("/cs2")
    public ResponseEntity<List<Map<String, Object>>> getCs2Leaderboard() {
        return getLeaderboard(Game.CS2);
    }

    private ResponseEntity<List<Map<String, Object>>> getLeaderboard(Game game) {
        List<Wallet> wallets = walletRepository.findAll();
        List<Map<String, Object>> entries = new ArrayList<>();

        for (Wallet wallet : wallets) {
            if (wallet.getUser() == null) continue;
            User user = wallet.getUser();
            RankTier lolRank = rankService.resolveRank(Game.LOL, wallet.getLolElo());
            RankTier cs2Rank = rankService.resolveRank(Game.CS2, wallet.getCs2Elo());
            entries.add(Map.of(
                    "userId", user.getId(),
                    "username", user.getUsername(),
                    "displayName", user.getDisplayName(),
                    "avatarUrl", user.getAvatarUrl(),
                    "lolElo", wallet.getLolElo(),
                    "cs2Elo", wallet.getCs2Elo(),
                    "lolRank", lolRank != null ? lolRank.getTierName() : null,
                    "cs2Rank", cs2Rank != null ? cs2Rank.getTierName() : null
            ));
        }

        String eloField = game == Game.LOL ? "lolElo" : "cs2Elo";
        entries.sort(Comparator.comparingInt(
                (Map<String, Object> e) -> (int) e.get(eloField)
        ).reversed());

        if (entries.size() > 50) {
            entries = entries.subList(0, 50);
        }

        int rankPosition = 1;
        for (Map<String, Object> entry : entries) {
            entry.put("rankPosition", rankPosition++);
        }

        return ResponseEntity.ok(entries);
    }
}
