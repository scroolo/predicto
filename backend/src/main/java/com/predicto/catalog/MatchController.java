package com.predicto.catalog;

import com.predicto.betting.OddsService;
import com.predicto.betting.dto.OddsResponse;
import com.predicto.catalog.dto.MatchResponse;
import com.predicto.common.enums.Game;
import com.predicto.common.enums.MatchStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/matches")
@RequiredArgsConstructor
public class MatchController {

    private final MatchRepository matchRepository;
    private final OddsService oddsService;

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<List<MatchResponse>> getMatches(
            @RequestParam(required = false) Game game,
            @RequestParam(required = false) UUID league,
            @RequestParam(required = false) MatchStatus status) {

        List<Match> matches;
        if (game != null && status != null) {
            matches = matchRepository.findByGameAndStatus(game, status);
        } else if (game != null) {
            matches = matchRepository.findByGame(game);
        } else if (league != null && status != null) {
            matches = matchRepository.findByLeagueIdAndStatus(league, status);
        } else if (league != null) {
            matches = matchRepository.findByLeagueId(league);
        } else if (status != null) {
            matches = matchRepository.findByStatus(status);
        } else {
            matches = matchRepository.findAll();
        }

        var response = matches.stream()
                .sorted((a, b) -> a.getStartsAt().compareTo(b.getStartsAt()))
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{matchId}")
    @Transactional(readOnly = true)
    public ResponseEntity<MatchResponse> getMatch(@PathVariable UUID matchId) {
        return matchRepository.findById(matchId)
                .map(m -> ResponseEntity.ok(toResponse(m)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{matchId}/odds")
    public ResponseEntity<OddsResponse> getOdds(@PathVariable UUID matchId) {
        return ResponseEntity.ok(oddsService.getOdds(matchId));
    }

    private MatchResponse toResponse(Match m) {
        MatchResponse.MatchResultRef result = null;
        if (m.getResultWinnerTeam() != null) {
            result = new MatchResponse.MatchResultRef(
                    m.getResultWinnerTeam().getId(),
                    m.getResultScore());
        }
        return new MatchResponse(
                m.getId(),
                m.getGame(),
                new MatchResponse.LeagueRef(m.getLeague().getId(), m.getLeague().getName(), m.getLeague().getLogoUrl()),
                new MatchResponse.TeamRef(m.getTeamA().getId(), m.getTeamA().getName(), m.getTeamA().getLogoUrl()),
                new MatchResponse.TeamRef(m.getTeamB().getId(), m.getTeamB().getName(), m.getTeamB().getLogoUrl()),
                m.getFormat(),
                m.getStage(),
                m.getStartsAt(),
                m.getStatus(),
                result
        );
    }
}
