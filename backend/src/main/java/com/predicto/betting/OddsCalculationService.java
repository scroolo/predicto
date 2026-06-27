package com.predicto.betting;

import com.predicto.auth.User;
import com.predicto.auth.UserRepository;
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
    private static final int HISTORY_LIMIT = 20;
    private static final int MIN_MATCHES_FOR_CALC = 3;
    private static final double MARGIN = 1.05;
    private static final double DEFAULT_ODDS = 1.90;
    private static final double MIN_ODDS = 1.10;
    private static final double MAX_ODDS = 9.99;

    private final MatchRepository matchRepository;
    private final MatchOddsRepository matchOddsRepository;
    private final UserRepository userRepository;

    private User adminUser;

    public OddsCalculationService(MatchRepository matchRepository,
                                   MatchOddsRepository matchOddsRepository,
                                   UserRepository userRepository) {
        this.matchRepository = matchRepository;
        this.matchOddsRepository = matchOddsRepository;
        this.userRepository = userRepository;
    }

    public void calculateAndSaveOdds(Match match) {
        if (match.getTeamA() == null || match.getTeamB() == null) return;

        resolveAdminUser();

        Team teamA = match.getTeamA();
        Team teamB = match.getTeamB();

        int teamATotal = countFinishedMatches(teamA.getId(), match.getId());
        int teamBTotal = countFinishedMatches(teamB.getId(), match.getId());

        double oddsA;
        double oddsB;

        if (teamATotal < MIN_MATCHES_FOR_CALC || teamBTotal < MIN_MATCHES_FOR_CALC) {
            oddsA = DEFAULT_ODDS;
            oddsB = DEFAULT_ODDS;
            log.info("Match {}: insufficient history (teamA={}, teamB={} matches), using default odds",
                match.getId(), teamATotal, teamBTotal);
        } else {
            int teamAWins = countWins(teamA.getId(), match.getId());
            double winRateA = Math.min(0.9, Math.max(0.1,
                (double) teamAWins / Math.min(teamATotal, HISTORY_LIMIT)));
            double winRateB = 1.0 - winRateA;

            oddsA = clamp(round(MARGIN / winRateA));
            oddsB = clamp(round(MARGIN / winRateB));

            log.info("Match {}: teamA wins={}/{} winRate={} odds={}, teamB odds={}",
                match.getId(), teamAWins, teamATotal, winRateA, oddsA, oddsB);
        }

        // Popularity adjustment based on current bets
        long betsOnA = matchOddsRepository.countBetsByTeamAndMatch(teamA.getId(), match.getId());
        long betsOnB = matchOddsRepository.countBetsByTeamAndMatch(teamB.getId(), match.getId());
        long totalBets = betsOnA + betsOnB;

        if (totalBets >= 5) {
            double popularityA = (double) betsOnA / totalBets;
            double popularityB = (double) betsOnB / totalBets;
            // If team is popular (>50% bets), reduce odds slightly; if unpopular, increase
            double adjustA = 1.0 + (0.5 - popularityA) * 0.3;
            double adjustB = 1.0 + (0.5 - popularityB) * 0.3;
            oddsA = clamp(round(oddsA * adjustA));
            oddsB = clamp(round(oddsB * adjustB));
            log.info("Match {}: popularity adjustment betsA={} betsB={} -> oddsA={} oddsB={}",
                match.getId(), betsOnA, betsOnB, oddsA, oddsB);
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

    private int countWins(UUID teamId, UUID excludeMatchId) {
        return matchRepository.countWinsByTeamId(teamId, excludeMatchId, HISTORY_LIMIT);
    }

    private int countFinishedMatches(UUID teamId, UUID excludeMatchId) {
        return matchRepository.countFinishedMatchesByTeamId(teamId, excludeMatchId, HISTORY_LIMIT);
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
