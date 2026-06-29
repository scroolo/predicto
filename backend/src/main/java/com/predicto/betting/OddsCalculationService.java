package com.predicto.betting;

import com.predicto.auth.UserRepository;
import com.predicto.betting.Bet;
import com.predicto.betting.BetRepository;
import com.predicto.catalog.Match;
import com.predicto.catalog.MatchRepository;
import com.predicto.catalog.Team;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final BetRepository betRepository;
    private final OddsSaverService oddsSaverService;

    public OddsCalculationService(MatchRepository matchRepository,
                                   MatchOddsRepository matchOddsRepository,
                                   BetRepository betRepository,
                                   OddsSaverService oddsSaverService) {
        this.matchRepository = matchRepository;
        this.matchOddsRepository = matchOddsRepository;
        this.betRepository = betRepository;
        this.oddsSaverService = oddsSaverService;
    }

    public void calculateAndSaveOdds(Match match) {
        if (match.getTeamA() == null || match.getTeamB() == null) return;

        Team teamA = match.getTeamA();
        Team teamB = match.getTeamB();

        double oddsA = 1.85;
        double oddsB = 1.85;

        List<Bet> betsA = betRepository.findPendingByMatchAndTeam(match.getId(), teamA.getId());
        List<Bet> betsB = betRepository.findPendingByMatchAndTeam(match.getId(), teamB.getId());

        double weightA = betsA.stream().mapToDouble(b -> Math.sqrt(b.getStake())).sum();
        double weightB = betsB.stream().mapToDouble(b -> Math.sqrt(b.getStake())).sum();
        double totalWeight = weightA + weightB;

        log.info("Match {}: betsA count={} betsB count={} weightA={} weightB={} totalWeight={}",
            match.getId(), betsA.size(), betsB.size(), weightA, weightB, totalWeight);

        if (totalWeight == 0) {
            boolean hasOddsA = matchOddsRepository.findByMatchIdAndTeamId(match.getId(), teamA.getId()).isPresent();
            if (!hasOddsA) {
                oddsSaverService.saveOdds(match, teamA, oddsA, teamB, oddsB);
            }
            return;
        }

        double popularityA = weightA / totalWeight;
        double popularityB = weightB / totalWeight;

        double adjustA = 1.0 + (0.5 - popularityA) * 0.60;
        double adjustB = 1.0 + (0.5 - popularityB) * 0.60;

        oddsA = clamp(round(oddsA * adjustA));
        oddsB = clamp(round(oddsB * adjustB));

        log.info("Match {}: popularityA={} popularityB={} -> oddsA={} oddsB={}",
            match.getId(), popularityA, popularityB, oddsA, oddsB);

        oddsSaverService.saveOdds(match, teamA, oddsA, teamB, oddsB);
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
