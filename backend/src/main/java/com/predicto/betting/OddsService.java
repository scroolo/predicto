package com.predicto.betting;

import com.predicto.auth.User;
import com.predicto.betting.dto.OddsResponse;
import com.predicto.betting.dto.SetOddsRequest;
import com.predicto.catalog.Match;
import com.predicto.catalog.MatchRepository;
import com.predicto.catalog.Team;
import com.predicto.catalog.TeamRepository;
import com.predicto.common.enums.MatchFormat;
import com.predicto.common.enums.MatchStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OddsService {

    private final MatchRepository matchRepository;
    private final MatchOddsRepository matchOddsRepository;
    private final ScoreOddsRepository scoreOddsRepository;
    private final TeamRepository teamRepository;

    private static final Map<MatchFormat, Set<String>> VALID_SCORES = Map.of(
            MatchFormat.BO1, Set.of("1:0", "0:1"),
            MatchFormat.BO3, Set.of("2:0", "2:1", "0:2", "1:2"),
            MatchFormat.BO5, Set.of("3:0", "3:1", "3:2", "0:3", "1:3", "2:3")
    );

    @Transactional
    public OddsResponse setOdds(UUID matchId, SetOddsRequest request, User admin) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("Match not found: " + matchId));

        if (match.getStatus() != MatchStatus.SCHEDULED) {
            throw new IllegalStateException(
                    "Cannot set odds on a match with status " + match.getStatus()
                            + " — only SCHEDULED matches accept odds"
            );
        }

        Set<UUID> matchTeamIds = Set.of(match.getTeamA().getId(), match.getTeamB().getId());
        for (var item : request.winnerOdds()) {
            if (!matchTeamIds.contains(item.teamId())) {
                throw new IllegalArgumentException("Team " + item.teamId() + " is not part of this match");
            }
        }

        Set<String> validScores = VALID_SCORES.get(match.getFormat());
        if (validScores == null) {
            throw new IllegalArgumentException("Unsupported match format: " + match.getFormat());
        }
        for (var item : request.scoreOdds()) {
            if (!validScores.contains(item.scoreValue())) {
                throw new IllegalArgumentException(
                        "Invalid score '" + item.scoreValue() + "' for format " + match.getFormat()
                );
            }
        }

        for (var item : request.winnerOdds()) {
            Team team = teamRepository.getReferenceById(item.teamId());
            MatchOdds odds = matchOddsRepository.findByMatchIdAndTeamId(matchId, item.teamId())
                    .orElse(MatchOdds.builder().match(match).team(team).build());
            odds.setOddsValue(item.oddsValue());
            odds.setSetByUser(admin);
            odds.setUpdatedAt(OffsetDateTime.now());
            matchOddsRepository.save(odds);
        }

        for (var item : request.scoreOdds()) {
            ScoreOdds odds = scoreOddsRepository.findByMatchIdAndScoreValue(matchId, item.scoreValue())
                    .orElse(ScoreOdds.builder().match(match).scoreValue(item.scoreValue()).build());
            odds.setOddsValue(item.oddsValue());
            odds.setSetByUser(admin);
            odds.setUpdatedAt(OffsetDateTime.now());
            scoreOddsRepository.save(odds);
        }

        return getOdds(matchId);
    }

    public OddsResponse getOdds(UUID matchId) {
        List<MatchOdds> winnerOdds = matchOddsRepository.findByMatchId(matchId);
        List<ScoreOdds> scoreOdds = scoreOddsRepository.findByMatchId(matchId);
        return OddsResponse.from(winnerOdds, scoreOdds);
    }
}
