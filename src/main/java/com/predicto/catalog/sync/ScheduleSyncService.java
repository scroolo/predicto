package com.predicto.catalog.sync;

import com.predicto.catalog.*;
import com.predicto.catalog.cito.CitoApiClient;
import com.predicto.catalog.cito.ScheduledMatchDto;
import com.predicto.common.enums.Game;
import com.predicto.common.enums.MatchFormat;
import com.predicto.common.enums.MatchStatus;
import com.predicto.common.enums.Source;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleSyncService {

    private final CitoApiClient citoClient;
    private final LeagueRepository leagueRepository;
    private final TeamRepository teamRepository;
    private final MatchRepository matchRepository;
    private final SyncRunnerService syncRunner;

    @Scheduled(fixedRateString = "${cito.sync.schedule-interval-ms:1800000}")
    public void syncSchedule() {
        SyncRun run = syncRunner.startRun("schedule-sync");
        int total = 0;
        try {
            total += doSync();
            syncRunner.completeRun(run, total);
        } catch (Exception e) {
            log.error("Schedule sync failed", e);
            syncRunner.failRun(run, e.getMessage());
        }
    }

    @Transactional
    public int doSync() {
        List<ScheduledMatchDto> matches = citoClient.fetchSchedule();
        int count = 0;
        for (ScheduledMatchDto dto : matches) {
            Match existing = matchRepository.findByExternalId(dto.matchId()).orElse(null);
            if (existing != null && existing.getStatus() != MatchStatus.SCHEDULED) {
                continue;
            }
            League league = leagueRepository.findByExternalId(dto.league()).orElse(null);
            if (league == null) {
                log.warn("Unknown league {} for match {}", dto.league(), dto.matchId());
                continue;
            }
            Team teamA = teamRepository.findByExternalId(dto.team1().slug()).orElse(null);
            Team teamB = teamRepository.findByExternalId(dto.team2().slug()).orElse(null);
            if (teamA == null || teamB == null) {
                log.warn("Unknown teams for match {}", dto.matchId());
                continue;
            }
            Match match = existing != null ? existing : Match.builder().build();
            match.setExternalId(dto.matchId());
            match.setSource(Source.API);
            match.setGame(Game.LOL);
            match.setLeague(league);
            match.setTeamA(teamA);
            match.setTeamB(teamB);
            match.setFormat(MatchFormat.valueOf(dto.format()));
            match.setStage(dto.stage());
            match.setStartsAt(OffsetDateTime.parse(dto.startTime()));
            if (existing == null) {
                match.setStatus(MatchStatus.SCHEDULED);
            }
            matchRepository.save(match);
            count++;
        }
        log.info("Synced {} matches", count);
        return count;
    }
}
