package com.predicto.achievement;

import com.predicto.auth.security.JwtUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/achievements")
@RequiredArgsConstructor
public class AchievementController {

    private final AchievementService achievementService;

    @GetMapping
    public ResponseEntity<List<Achievement>> getAllAchievements() {
        return ResponseEntity.ok(achievementService.getAllAchievements());
    }

    @GetMapping("/me")
    public ResponseEntity<List<UserAchievement>> getMyAchievements(@AuthenticationPrincipal JwtUser jwtUser) {
        if (jwtUser == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(achievementService.getUserAchievements(jwtUser.id()));
    }
}
