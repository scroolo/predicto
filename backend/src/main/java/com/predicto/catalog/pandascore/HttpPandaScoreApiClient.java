package com.predicto.catalog.pandascore;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Slf4j

@Component
@ConditionalOnProperty(name = "pandascore.mock-enabled", havingValue = "false")
public class HttpPandaScoreApiClient implements PandaScoreApiClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public HttpPandaScoreApiClient(
            @Value("${pandascore.token:}") String token,
            ObjectMapper objectMapper
    ) {
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(10))
                        .build()
        );
        factory.setReadTimeout(Duration.ofSeconds(30));
        this.restClient = RestClient.builder()
                .baseUrl("https://api.pandascore.co")
                .defaultHeader("Authorization", "Bearer " + token)
                .requestFactory(factory)
                .build();
        this.objectMapper = objectMapper;
    }

    @Override
    public List<PandaScoreMatch> fetchLolUpcoming() {
        return fetchListDebug("/lol/matches/upcoming?page[size]=25", PandaScoreMatch.class);
    }

    @Override
    public List<PandaScoreMatch> fetchLolRunning() {
        return fetchListOnePage("/lol/matches/running?page[size]=25", PandaScoreMatch.class);
    }

    @Override
    public List<PandaScoreMatch> fetchLolPast() {
        return fetchListOnePage("/lol/matches/past?page[size]=25", PandaScoreMatch.class);
    }

    @Override
    public List<PandaScoreLeague> fetchLolLeagues() {
        return fetchList("/lol/leagues?page[size]=25", PandaScoreLeague.class);
    }

    @Override
    public List<PandaScoreTeam> fetchLolTeams() {
        return fetchList("/lol/teams?page[size]=25", PandaScoreTeam.class);
    }

    @Override
    public List<PandaScoreMatch> fetchCs2Upcoming() {
        return fetchListDebug("/csgo/matches/upcoming?page[size]=25", PandaScoreMatch.class);
    }

    @Override
    public List<PandaScoreMatch> fetchCs2Running() {
        return fetchListOnePage("/csgo/matches/running?page[size]=25", PandaScoreMatch.class);
    }

    @Override
    public List<PandaScoreMatch> fetchCs2Past() {
        return fetchListOnePage("/csgo/matches/past?page[size]=25", PandaScoreMatch.class);
    }

    @Override
    public List<PandaScoreMatch> fetchPastMatches(String game, int page, int perPage) {
        String endpoint = game.equals("LOL") ? "/lol/matches/past" : "/csgo/matches/past";
        String path = endpoint + "?page[size]=" + perPage + "&page[number]=" + page + "&sort=-begin_at";
        String json = restClient.get()
                .uri(path)
                .retrieve()
                .body(String.class);
        return deserializeList(json, path, PandaScoreMatch.class);
    }

    @Override
    public List<PandaScoreLeague> fetchCs2Leagues() {
        return fetchList("/csgo/leagues?page[size]=25", PandaScoreLeague.class);
    }

    @Override
    public List<PandaScoreTeam> fetchCs2Teams() {
        return fetchList("/csgo/teams?page[size]=25", PandaScoreTeam.class);
    }

    private static final int PAGE_SIZE = 25;
    private static final int MAX_PAGINATION_PAGES = 50;

    private <T> List<T> fetchList(String path, Class<T> elementType) {
        return fetchAllPages(path, elementType, false, MAX_PAGINATION_PAGES);
    }

    private <T> List<T> fetchListDebug(String path, Class<T> elementType) {
        return fetchAllPages(path, elementType, true, MAX_PAGINATION_PAGES);
    }

    private <T> List<T> fetchListOnePage(String path, Class<T> elementType) {
        return fetchAllPages(path, elementType, false, 1);
    }

    private <T> List<T> fetchAllPages(String path, Class<T> elementType, boolean debug, int maxPages) {
        List<T> allResults = new ArrayList<>();
        int page = 1;
        String basePath = path.contains("?") ? path.substring(0, path.indexOf("?")) : path;
        while (page <= maxPages) {
            String pagePath = basePath + "?page[size]=" + PAGE_SIZE + "&page[number]=" + page;
            String json = restClient.get()
                    .uri(pagePath)
                    .retrieve()
                    .body(String.class);
            if (debug && page == 1) {
                log.info("PandaScore raw response from {} (first 500 chars): {}", pagePath,
                        json.substring(0, Math.min(500, json.length())));
            }
            List<T> pageResults = deserializeList(json, pagePath, elementType);
            if (pageResults.isEmpty()) break;
            allResults.addAll(pageResults);
            page++;
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        return allResults;
    }

    private <T> List<T> deserializeList(String json, String path, Class<T> elementType) {
        JavaType type = objectMapper.getTypeFactory()
                .constructCollectionType(List.class, elementType);
        try {
            return objectMapper.readValue(json, type);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize PandaScore response from " + path, e);
        }
    }
}
