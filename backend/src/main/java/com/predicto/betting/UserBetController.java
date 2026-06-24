package com.predicto.betting;

import com.predicto.auth.User;
import com.predicto.auth.UserRepository;
import com.predicto.auth.security.JwtUser;
import com.predicto.betting.dto.BetResponse;
import com.predicto.betting.dto.PlaceBetRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class UserBetController {

    private final BettingService bettingService;
    private final UserRepository userRepository;

    @PostMapping("/api/matches/{matchId}/bets")
    public ResponseEntity<?> placeBet(
            @PathVariable UUID matchId,
            @Valid @RequestBody PlaceBetRequest request) {
        User user = currentUser();
        try {
            BetResponse response = bettingService.placeBet(matchId, request, user);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/api/matches/{matchId}/bets")
    public ResponseEntity<?> cancelBet(@PathVariable UUID matchId) {
        User user = currentUser();
        try {
            bettingService.cancelBet(matchId, user);
            return ResponseEntity.ok(Map.of("status", "cancelled"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/api/users/me/bets")
    public ResponseEntity<List<BetResponse>> myBets() {
        User user = currentUser();
        return ResponseEntity.ok(bettingService.getUserBets(user.getId()));
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
