package com.predicto.admin;

import com.predicto.auth.User;
import com.predicto.auth.UserRepository;
import com.predicto.auth.security.JwtUser;
import com.predicto.betting.OddsService;
import com.predicto.betting.SettlementService;
import com.predicto.betting.dto.ConfirmResultRequest;
import com.predicto.betting.dto.OddsResponse;
import com.predicto.betting.dto.SetOddsRequest;
import com.predicto.catalog.Match;
import com.predicto.catalog.MatchRepository;
import com.predicto.catalog.Player;
import com.predicto.catalog.PlayerRepository;
import com.predicto.catalog.Team;
import com.predicto.catalog.TeamRepository;
import com.predicto.common.enums.MatchStatus;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/matches")
@RequiredArgsConstructor
public class AdminBettingController {

    private final MatchRepository matchRepository;
    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;
    private final OddsService oddsService;
    private final SettlementService settlementService;
    private final UserRepository userRepository;

    @PutMapping("/{matchId}/odds")
    public ResponseEntity<?> setOdds(
            @PathVariable UUID matchId,
            @Valid @RequestBody SetOddsRequest request,
            HttpServletRequest httpReq) {
        System.out.println("ODDS UPDATE: principal=" + httpReq.getUserPrincipal() + " auth=" + SecurityContextHolder.getContext().getAuthentication());
        User admin = currentUser();
        try {
            OddsResponse response = oddsService.setOdds(matchId, request, admin);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/{matchId}/confirm-result")
    @Transactional
    public ResponseEntity<?> confirmResult(
            @PathVariable UUID matchId,
            @Valid @RequestBody ConfirmResultRequest request) {
        currentUser();
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("Match not found: " + matchId));

        if (match.getStatus() == MatchStatus.SCHEDULED) {
            return ResponseEntity.status(409)
                    .body(Map.of("message", "Cannot confirm result on a SCHEDULED match"));
        }

        if (!match.getTeamA().getId().equals(request.winnerTeamId())
                && !match.getTeamB().getId().equals(request.winnerTeamId())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Winner team is not part of this match"));
        }

        Team winnerTeam = teamRepository.getReferenceById(request.winnerTeamId());
        match.setResultWinnerTeam(winnerTeam);
        match.setResultScore(request.score());
        if (request.mvpPlayerId() != null) {
            Player mvp = playerRepository.getReferenceById(request.mvpPlayerId());
            match.setResultMvpPlayer(mvp);
        }
        match.setStatus(MatchStatus.FINISHED);
        match.setResultConfirmedAt(OffsetDateTime.now());
        matchRepository.save(match);

        settlementService.settleMatch(match);

        return ResponseEntity.ok(Map.of(
                "status", "settled",
                "matchId", matchId
        ));
    }

    @PostMapping("/{matchId}/cancel")
    @Transactional
    public ResponseEntity<?> cancelMatch(@PathVariable UUID matchId) {
        currentUser();
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("Match not found: " + matchId));

        match.setStatus(MatchStatus.CANCELLED);
        matchRepository.save(match);

        settlementService.voidMatchBets(match);

        return ResponseEntity.ok(Map.of(
                "status", "cancelled",
                "matchId", matchId
        ));
    }

    private User currentUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof JwtUser jwtUser)) {
            throw new IllegalStateException("Unauthorized");
        }
        return userRepository.findById(jwtUser.id())
                .orElseThrow(() -> new IllegalStateException("User not found"));
    }
}
