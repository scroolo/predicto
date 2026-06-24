package com.predicto.catalog;

import com.predicto.catalog.dto.LeagueResponse;
import com.predicto.common.enums.Game;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/leagues")
@RequiredArgsConstructor
@Slf4j
public class LeagueController {

    private final LeagueRepository leagueRepository;

    @GetMapping
    public ResponseEntity<List<LeagueResponse>> getLeagues(
            @RequestParam(required = false) String game
    ) {
        log.info("League filter game param: {}", game);
        List<League> leagues;
        if (game != null && !game.isBlank()) {
            try {
                leagues = leagueRepository.findByGame(Game.valueOf(game));
            } catch (IllegalArgumentException e) {
                log.warn("Invalid game param: {}, returning all leagues", game);
                leagues = leagueRepository.findAll();
            }
        } else {
            leagues = leagueRepository.findAll();
        }
        var response = leagues.stream()
                .map(l -> new LeagueResponse(l.getId(), l.getName(), l.getRegion(), l.getLogoUrl()))
                .sorted((a, b) -> a.name().compareToIgnoreCase(b.name()))
                .toList();
        return ResponseEntity.ok(response);
    }
}
