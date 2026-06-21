package com.predicto.catalog.cito;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
@ConditionalOnProperty(name = "cito.api.mock-enabled", havingValue = "false")
public class HttpCitoApiClient implements CitoApiClient {

    private final RestClient restClient;

    public HttpCitoApiClient(
            @Value("${cito.api.base-url}") String baseUrl,
            @Value("${cito.api.api-key}") String apiKey
    ) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("x-api-key", apiKey)
                .build();
    }

    @Override
    public List<LeagueDto> fetchLeagues() {
        return restClient.get()
                .uri("/leagues")
                .retrieve()
                .body(new ParameterizedTypeReference<CitoEnvelope<List<LeagueDto>>>() {})
                .data();
    }

    @Override
    public List<TeamDto> fetchTeamsByLeague(String leagueId) {
        return restClient.get()
                .uri("/leagues/{leagueId}/teams", leagueId)
                .retrieve()
                .body(new ParameterizedTypeReference<CitoEnvelope<List<TeamDto>>>() {})
                .data();
    }

    @Override
    public RosterDto fetchRosterByTeam(String teamSlug) {
        return restClient.get()
                .uri("/teams/{teamSlug}/roster", teamSlug)
                .retrieve()
                .body(new ParameterizedTypeReference<CitoEnvelope<RosterDto>>() {})
                .data();
    }

    @Override
    public List<ScheduledMatchDto> fetchSchedule() {
        return restClient.get()
                .uri("/matches")
                .retrieve()
                .body(new ParameterizedTypeReference<CitoEnvelope<List<ScheduledMatchDto>>>() {})
                .data();
    }

    @Override
    public ScheduledMatchDto fetchMatchDetail(String matchId) {
        return restClient.get()
                .uri("/matches/{matchId}", matchId)
                .retrieve()
                .body(new ParameterizedTypeReference<CitoEnvelope<ScheduledMatchDto>>() {})
                .data();
    }
}
