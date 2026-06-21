package com.predicto.catalog.cito;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ConditionalOnProperty(name = "cito.api.mock-enabled", havingValue = "true", matchIfMissing = true)
public class MockCitoApiClient implements CitoApiClient {

    private final ObjectMapper objectMapper;
    private final ResourceLoader resourceLoader;

    private List<LeagueDto> leagues;
    private final Map<String, List<TeamDto>> teamsByLeague = new ConcurrentHashMap<>();
    private final Map<String, RosterDto> rostersByTeam = new ConcurrentHashMap<>();

    @Autowired
    public MockCitoApiClient(ObjectMapper objectMapper, ResourceLoader resourceLoader) {
        this.objectMapper = objectMapper;
        this.resourceLoader = resourceLoader;
    }

    @PostConstruct
    public void init() {
        try {
            loadLeagues();
            loadTeams();
            loadRosters();
        } catch (Exception e) {
            throw new RuntimeException("Failed to load mock fixtures", e);
        }
    }

    private void loadLeagues() throws Exception {
        try (InputStream is = resourceLoader.getResource("classpath:cito-fixtures/leagues.json").getInputStream()) {
            CitoEnvelope<List<LeagueDto>> envelope = objectMapper.readValue(is,
                    new TypeReference<CitoEnvelope<List<LeagueDto>>>() {});
            this.leagues = Collections.unmodifiableList(envelope.data());
        }
    }

    private void loadTeams() throws Exception {
        try (InputStream is = resourceLoader.getResource("classpath:cito-fixtures/teams-by-league.json").getInputStream()) {
            Map<String, CitoEnvelope<List<TeamDto>>> raw = objectMapper.readValue(is,
                    new TypeReference<Map<String, CitoEnvelope<List<TeamDto>>>>() {});
            raw.forEach((leagueId, envelope) ->
                    teamsByLeague.put(leagueId, Collections.unmodifiableList(envelope.data())));
        }
    }

    private void loadRosters() throws Exception {
        try (InputStream is = resourceLoader.getResource("classpath:cito-fixtures/rosters-by-team.json").getInputStream()) {
            Map<String, CitoEnvelope<RosterDto>> raw = objectMapper.readValue(is,
                    new TypeReference<Map<String, CitoEnvelope<RosterDto>>>() {});
            raw.forEach((teamSlug, envelope) ->
                    rostersByTeam.put(teamSlug, envelope.data()));
        }
    }

    @Override
    public List<LeagueDto> fetchLeagues() {
        return leagues;
    }

    @Override
    public List<TeamDto> fetchTeamsByLeague(String leagueId) {
        return teamsByLeague.getOrDefault(leagueId, List.of());
    }

    @Override
    public RosterDto fetchRosterByTeam(String teamSlug) {
        return rostersByTeam.get(teamSlug);
    }

    @Override
    public List<ScheduledMatchDto> fetchSchedule() {
        Instant now = Instant.now();
        return List.of(
                createMatch("mock-lol-match-001", "lck",
                        "t1", "T1", "geng", "Gen.G",
                        now.minus(2, ChronoUnit.DAYS),
                        "BO3", "Regular Season", "completed",
                        new MatchResultDto("t1", "2:1")),
                createMatch("mock-lol-match-002", "lck",
                        "hle", "Hanwha Life Esports", "geng", "Gen.G",
                        now.plus(5, ChronoUnit.MINUTES),
                        "BO3", "Regular Season", "scheduled",
                        null),
                createMatch("mock-lol-match-003", "lec",
                        "g2", "G2 Esports", "fnc", "Fnatic",
                        now.plus(3, ChronoUnit.DAYS),
                        "BO3", "Playoffs", "scheduled",
                        null),
                createMatch("mock-lol-match-004", "lcs",
                        "tl", "Team Liquid", "c9", "Cloud9",
                        now.plus(5, ChronoUnit.DAYS),
                        "BO5", "Grand Final", "scheduled",
                        null),
                createMatch("mock-lol-match-005", "lpl",
                        "blg", "Bilibili Gaming", "tes", "Top Esports",
                        now.plus(7, ChronoUnit.DAYS),
                        "BO3", "Regular Season", "scheduled",
                        null)
        );
    }

    @Override
    public ScheduledMatchDto fetchMatchDetail(String matchId) {
        return fetchSchedule().stream()
                .filter(m -> m.matchId().equals(matchId))
                .findFirst()
                .orElse(null);
    }

    private ScheduledMatchDto createMatch(String matchId, String league,
                                           String team1Slug, String team1Name,
                                           String team2Slug, String team2Name,
                                           Instant startTime, String format,
                                           String stage, String status,
                                           MatchResultDto result) {
        return new ScheduledMatchDto(
                matchId,
                league,
                new MatchTeamDto(team1Slug, team1Name),
                new MatchTeamDto(team2Slug, team2Name),
                startTime.toString(),
                format,
                stage,
                status,
                result
        );
    }
}
