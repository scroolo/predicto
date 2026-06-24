package com.predicto.admin;

import com.predicto.betting.BetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class AdminBetOverviewController {

    private final BetRepository betRepository;

    @GetMapping("/api/admin/bets")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Map<String, Object>>> getAllBets() {
        var bets = betRepository.findAll();
        var result = new ArrayList<Map<String, Object>>();
        for (var bet : bets) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", bet.getId());
            map.put("userId", bet.getUser().getId());
            map.put("username", bet.getUser().getUsername());
            map.put("matchId", bet.getMatch().getId());
            map.put("game", bet.getMatch().getGame().name());
            map.put("leagueName", bet.getMatch().getLeague().getName());
            map.put("teamAName", bet.getMatch().getTeamA().getName());
            map.put("teamBName", bet.getMatch().getTeamB().getName());
            map.put("stake", bet.getStake());
            map.put("potentialReturn", bet.getPotentialReturn());
            map.put("status", bet.getStatus().name());
            map.put("pointsAwarded", bet.getPointsAwarded());
            map.put("actualReturn", bet.getActualReturn());
            map.put("createdAt", bet.getCreatedAt());
            map.put("settledAt", bet.getSettledAt());
            result.add(map);
        }
        return ResponseEntity.ok(result);
    }
}
