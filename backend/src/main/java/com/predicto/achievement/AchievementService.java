package com.predicto.achievement;

import com.predicto.auth.User;
import com.predicto.auth.UserRepository;
import com.predicto.betting.BetRepository;
import com.predicto.common.enums.BetStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Slf4j
public class AchievementService {

    private final AchievementRepository achievementRepository;
    private final UserAchievementRepository userAchievementRepository;
    private final BetRepository betRepository;
    private final UserRepository userRepository;

    @Transactional
    public void checkAndAward(UUID userId, String trigger) {
        var user = userRepository.findById(userId).orElse(null);
        if (user == null) return;

        log.info("Achievement check: userId={}, trigger={}", userId, trigger);

        var allBets = betRepository.findByUserId(userId);
        long totalBets = allBets.size();
        long wonBets = allBets.stream().filter(b -> b.getPointsAwarded() > 0).count();
        long currentStreak = calculateWinStreak(allBets);

        switch (trigger) {
            case "bet_placed" -> {
                tryAward(userId, "first_prediction", totalBets >= 1);
                tryAward(userId, "predictions_10", totalBets >= 10);
                tryAward(userId, "predictions_100", totalBets >= 100);
                tryAward(userId, "predictions_1000", totalBets >= 1000);

                var firstBet = allBets.stream().findFirst().orElse(null);
                if (firstBet != null) {
                    String game = firstBet.getMatch().getGame().name();
                    if (game.equals("LOL")) tryAward(userId, "first_lol", true);
                    if (game.equals("CS2")) tryAward(userId, "first_cs2", true);
                }
            }
            case "bet_settled" -> {
                tryAward(userId, "hot_streak", currentStreak >= 5);
                tryAward(userId, "on_fire", currentStreak >= 10);
                tryAward(userId, "legendary_streak", currentStreak >= 25);
                tryAward(userId, "exact_score", hasExactScore(allBets));
            }
            case "daily_login" -> {
                tryAward(userId, "day_1", true);
                if (user.getCreatedAt() != null) {
                    long days = Duration.between(user.getCreatedAt(), OffsetDateTime.now()).toDays();
                    if (days >= 7) tryAward(userId, "week_1", true);
                    if (days >= 30) tryAward(userId, "month_1", true);
                    if (days >= 100) tryAward(userId, "days_100", true);
                    if (days >= 365) tryAward(userId, "days_365", true);
                }
            }
            case "leaderboard_update" -> {
                // These are awarded externally based on rank
            }
            case "f1_prediction" -> {
                tryAward(userId, "first_f1", true);
            }
        }
    }

    public void awardById(UUID userId, String achievementId) {
        tryAward(userId, achievementId, true);
    }

    private void tryAward(UUID userId, String achievementId, boolean condition) {
        if (!condition) return;
        if (userAchievementRepository.findByUserIdAndAchievementId(userId, achievementId).isPresent()) return;
        var achievement = achievementRepository.findById(achievementId).orElse(null);
        if (achievement == null) return;
        var ua = new UserAchievement();
        ua.setUserId(userId);
        ua.setAchievement(achievement);
        userAchievementRepository.save(ua);
    }

    private long calculateWinStreak(List<com.predicto.betting.Bet> bets) {
        var sorted = bets.stream()
                .filter(b -> b.getStatus() == BetStatus.WON || b.getStatus() == BetStatus.LOST)
                .sorted((a, b) -> b.getSettledAt().compareTo(a.getSettledAt()))
                .toList();
        long streak = 0;
        for (var bet : sorted) {
            if (bet.getPointsAwarded() > 0) streak++;
            else break;
        }
        return streak;
    }

    private boolean hasExactScore(List<com.predicto.betting.Bet> bets) {
        return bets.stream().anyMatch(b -> b.getPointsAwarded() > 0 && b.getExactScore() != null);
    }

    public List<Achievement> getAllAchievements() {
        return achievementRepository.findAll();
    }

    public List<UserAchievement> getUserAchievements(UUID userId) {
        return userAchievementRepository.findByUserId(userId);
    }
}
