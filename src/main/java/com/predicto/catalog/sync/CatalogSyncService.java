package com.predicto.catalog.sync;

import com.predicto.catalog.*;
import com.predicto.catalog.cito.CitoApiClient;
import com.predicto.catalog.cito.LeagueDto;
import com.predicto.catalog.cito.RosterEntryDto;
import com.predicto.catalog.cito.TeamDto;
import com.predicto.common.enums.Game;
import com.predicto.common.enums.Source;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CatalogSyncService {

    private final CitoApiClient citoClient;
    private final LeagueRepository leagueRepository;
    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;
    private final SyncRunnerService syncRunner;

    @Scheduled(fixedRateString = "${cito.sync.catalog-interval-ms:43200000}")
    public void syncAll() {
        SyncRun run = syncRunner.startRun("catalog-sync");
        int total = 0;
        try {
            total += syncLeagues();
            total += syncTeams();
            total += syncPlayers();
            syncRunner.completeRun(run, total);
        } catch (Exception e) {
            log.error("Catalog sync failed", e);
            syncRunner.failRun(run, e.getMessage());
        }
    }

    @Transactional
    public int syncLeagues() {
        List<LeagueDto> dtos = citoClient.fetchLeagues();
        int count = 0;
        for (LeagueDto dto : dtos) {
            League league = leagueRepository.findByExternalId(dto.id())
                    .orElse(League.builder().build());
            league.setExternalId(dto.id());
            league.setSource(Source.API);
            league.setGame(Game.LOL);
            league.setName(dto.name());
            league.setRegion(dto.region());
            league.setLogoUrl(dto.logoUrl());
            leagueRepository.save(league);
            count++;
        }
        log.info("Synced {} leagues", count);
        return count;
    }

    @Transactional
    public int syncTeams() {
        List<League> lolLeagues = leagueRepository.findByGame(Game.LOL);
        int count = 0;
        for (League league : lolLeagues) {
            List<TeamDto> dtos = citoClient.fetchTeamsByLeague(league.getExternalId());
            for (TeamDto dto : dtos) {
                Team team = teamRepository.findByExternalId(dto.slug())
                        .orElse(Team.builder().build());
                team.setExternalId(dto.slug());
                team.setSource(Source.API);
                team.setLeague(league);
                team.setGame(Game.LOL);
                team.setName(dto.name());
                team.setLogoUrl(dto.logoUrl());
                teamRepository.save(team);
                count++;
            }
        }
        log.info("Synced {} teams", count);
        return count;
    }

    @Transactional
    public int syncPlayers() {
        List<Team> lolTeams = teamRepository.findByGame(Game.LOL);
        int count = 0;
        for (Team team : lolTeams) {
            var roster = citoClient.fetchRosterByTeam(team.getExternalId());
            if (roster == null || roster.roster() == null) continue;
            for (RosterEntryDto entry : roster.roster()) {
                Player player = playerRepository.findByExternalId(entry.playerId())
                        .orElse(Player.builder().build());
                player.setExternalId(entry.playerId());
                player.setSource(Source.API);
                player.setTeam(team);
                player.setNickname(entry.nickname());
                player.setRole(entry.role());
                player.setPhotoUrl(entry.photoUrl());
                playerRepository.save(player);
                count++;
            }
        }
        log.info("Synced {} players", count);
        return count;
    }
}
