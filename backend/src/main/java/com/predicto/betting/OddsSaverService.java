package com.predicto.betting;

import com.predicto.auth.User;
import com.predicto.auth.UserRepository;
import com.predicto.catalog.Match;
import com.predicto.catalog.Team;
import com.predicto.common.enums.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OddsSaverService {

    private final MatchOddsRepository matchOddsRepository;
    private final UserRepository userRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveOdds(Match match, Team teamA, double oddsA, Team teamB, double oddsB) {
        List<User> admins = userRepository.findByRole(com.predicto.common.enums.UserRole.ADMIN);
        User adminUser = admins.isEmpty() ? null : admins.get(0);

        saveTeamOdds(match, teamA, oddsA, adminUser);
        saveTeamOdds(match, teamB, oddsB, adminUser);
    }

    private void saveTeamOdds(Match match, Team team, double oddsValue, User adminUser) {
        MatchOdds odds = matchOddsRepository.findByMatchIdAndTeamId(match.getId(), team.getId())
            .orElse(MatchOdds.builder()
                .match(match)
                .team(team)
                .build());
        odds.setOddsValue(BigDecimal.valueOf(oddsValue));
        odds.setSetByUser(adminUser);
        odds.setUpdatedAt(OffsetDateTime.now());
        matchOddsRepository.save(odds);
        log.info("OddsSaver: saved match={} team={} odds={}", match.getId(), team.getId(), oddsValue);
    }
}
