package com.predicto.catalog;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;

    @GetMapping("/{id}/players")
    public ResponseEntity<?> getPlayersByTeam(@PathVariable UUID id) {
        var team = teamRepository.findById(id).orElse(null);
        if (team == null) return ResponseEntity.notFound().build();
        var players = playerRepository.findByTeamId(id);
        var result = players.stream().map(p -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", p.getId());
            map.put("nickname", p.getNickname());
            map.put("role", p.getRole());
            map.put("photoUrl", p.getPhotoUrl());
            return map;
        }).toList();
        return ResponseEntity.ok(result);
    }
}
