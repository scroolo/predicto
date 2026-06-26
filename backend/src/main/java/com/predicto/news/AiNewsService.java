package com.predicto.news;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.net.http.*;
import java.net.URI;
import java.util.Map;

@Slf4j
@Service
public class AiNewsService {

    @Value("${anthropic.api.key:}")
    private String apiKey;

    private final ObjectMapper mapper = new ObjectMapper();

    public String generateArticle(RssItem item) throws Exception {
        String prompt = """
            Napíš esports/sports článok v slovenčine na základe tejto správy:
            
            Titulok: %s
            Zdroj: %s
            Popis: %s
            Odkaz: %s
            
            Štruktúra článku:
            1. TL;DR - krátke zhrnutie (2-3 vety)
            2. Čo sa stalo? (hlavná správa)
            3. Prečo je to dôležité?
            4. Kontext a história
            5. Dopad na scénu
            6. Predicto Insight (analytický pohľad, nie fakt)
            
            Dôležité:
            - Píš v slovenčine
            - Buď objektívny a faktický
            - Predicto Insight jasne označ ako analytický pohľad
            - Maximálne 500 slov
            - Vráť iba text článku bez markdown hlavičiek
            """.formatted(item.title(), item.source().name(), item.description(), item.link());

        var request = Map.of(
            "model", "claude-sonnet-4-6",
            "max_tokens", 1000,
            "messages", new Object[]{Map.of("role", "user", "content", prompt)}
        );

        HttpRequest httpRequest = HttpRequest.newBuilder()
            .uri(URI.create("https://api.anthropic.com/v1/messages"))
            .header("Content-Type", "application/json")
            .header("x-api-key", apiKey)
            .header("anthropic-version", "2023-06-01")
            .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(request)))
            .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
            .send(httpRequest, HttpResponse.BodyHandlers.ofString());

        String responseBody = response.body();
        log.info("Anthropic API status: {}", response.statusCode());
        log.info("Anthropic API response: {}", responseBody);

        var responseMap = mapper.readValue(responseBody, Map.class);
        var content = (java.util.List<?>) responseMap.get("content");
        var firstBlock = (Map<?, ?>) content.get(0);
        return (String) firstBlock.get("text");
    }
}
