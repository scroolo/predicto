package com.predicto.betting;

import com.predicto.auth.User;
import com.predicto.auth.UserRepository;
import com.predicto.betting.Bet;
import com.predicto.betting.BetRepository;
import com.predicto.catalog.Match;
import com.predicto.catalog.MatchRepository;
import com.predicto.catalog.Team;
import com.predicto.common.enums.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class OddsCalculationService {

    private static final Logger log = LoggerFactory.getLogger(OddsCalculationService.class);
    private static final double MIN_ODDS = 1.10;
    private static final double MAX_ODDS = 9.99;

    private final MatchRepository matchRepository;
    private final MatchOddsRepository matchOddsRepository;
    private final UserRepository userRepository;
    private final BetRepository betRepository;

    private User adminUser;

    public OddsCalculationService(MatchRepository matchRepository,
                                   MatchOddsRepository matchOddsRepository,
                                   UserRepository userRepository,
                                   BetRepository betRepository) {
        this.matchRepository = matchRepository;
        this.matchOddsRepository = matchOddsRepository;
        this.userRepository = userRepository;
        this.betRepository = betRepository;
    }

    public void calculateAndSaveOdds(Match match) {
        if (match.getTeamA() == null || match.getTeamB() == null) return;
        resolveAdminUser();

        Team teamA = match.getTeamA();
        Team teamB = match.getTeamB();

        // Base odds
        double oddsA = 1.85;
        double oddsB = 1.85;

        // Fetch current bets — weighted by sqrt(stake) to prevent whale manipulation
        List<Bet> betsA = betRepository.findPendingByMatchAndTeam(match.getId(), teamA.getId());
        List<Bet> betsB = betRepository.findPendingByMatchAndTeam(match.getId(), teamB.getId());

        double weightA = betsA.stream().mapToDouble(b -> Math.sqrt(b.getStake())).sum();
        double weightB = betsB.stream().mapToDouble(b -> Math.sqrt(b.getStake())).sum();
        double totalWeight = weightA + weightB;

        if (totalWeight > 0 && (betsA.size() + betsB.size()) >= 3) {
            double popularityA = weightA / totalWeight;
            double popularityB = weightB / totalWeight;

            // Max adjustment ±0.30 from base
            double adjustA = 1.0 + (0.5 - popularityA) * 0.60;
            double adjustB = 1.0 + (0.5 - popularityB) * 0.60;

            oddsA = clamp(round(oddsA * adjustA));
            oddsB = clamp(round(oddsB * adjustB));

            log.info("Match {}: betsA={} weightA={} betsB={} weightB={} -> oddsA={} oddsB={}",
                match.getId(), betsA.size(), weightA, betsB.size(), weightB, oddsA, oddsB);
        }

        saveOddsForTeam(match, teamA, oddsA);
        saveOddsForTeam(match, teamB, oddsB);
    }

    private void saveOddsForTeam(Match match, Team team, double oddsValue) {
        MatchOdds odds = matchOddsRepository.findByMatchIdAndTeamId(match.getId(), team.getId())
            .orElse(MatchOdds.builder()
                .match(match)
                .team(team)
                .build());
        odds.setOddsValue(BigDecimal.valueOf(oddsValue));
        odds.setSetByUser(adminUser);
        odds.setUpdatedAt(OffsetDateTime.now());
        matchOddsRepository.save(odds);
    }

    private void resolveAdminUser() {
        if (adminUser != null) return;
        List<User> admins = userRepository.findByRole(UserRole.ADMIN);
        if (admins.isEmpty()) {
            throw new IllegalStateException("No ADMIN user found — cannot set odds setByUser");
        }
        adminUser = admins.get(0);
    }

    private static double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private static double clamp(double value) {
        return Math.min(MAX_ODDS, Math.max(MIN_ODDS, value));
    }

    public int calculateOddsForAllUpcomingMatches() {
        List<Match> upcoming = matchRepository.findAllUpcoming();
        int count = 0;
        for (Match match : upcoming) {
            try {
                calculateAndSaveOdds(match);
                count++;
            } catch (Exception e) {
                log.warn("Failed to calculate odds for match {}: {}", match.getId(), e.getMessage());
            }
        }
        log.info("Auto-odds: calculated odds for {} matches", count);
        return count;
    }

    @Scheduled(cron = "0 30 */2 * * *")
    public void scheduledOddsSync() {
        log.info("Starting scheduled odds calculation...");
        int count = calculateOddsForAllUpcomingMatches();
        log.info("Scheduled odds calculation complete: {} matches updated", count);
    }
}
