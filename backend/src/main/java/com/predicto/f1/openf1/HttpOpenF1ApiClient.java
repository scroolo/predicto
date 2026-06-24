package com.predicto.f1.openf1;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Collections;
import java.util.List;

import static java.lang.Thread.sleep;

@Slf4j
@Component
public class HttpOpenF1ApiClient implements OpenF1ApiClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private volatile long lastRequestTime = 0L;

    public HttpOpenF1ApiClient(
            @Value("${openf1.api.base-url}") String baseUrl,
            ObjectMapper objectMapper
    ) {
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(10))
                        .build()
        );
        factory.setReadTimeout(Duration.ofSeconds(30));
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .build();
        this.objectMapper = objectMapper;
    }

    @Override
    public List<OpenF1Meeting> fetchMeetings(int year) {
        return fetchList("/meetings?year=" + year, OpenF1Meeting.class);
    }

    @Override
    public List<OpenF1Session> fetchSessions(int meetingKey) {
        return fetchList("/sessions?meeting_key=" + meetingKey, OpenF1Session.class);
    }

    @Override
    public List<OpenF1Session> fetchSessionsByYear(int year) {
        return fetchList("/sessions?year=" + year, OpenF1Session.class);
    }

    @Override
    public List<OpenF1Driver> fetchDrivers(int sessionKey) {
        return fetchList("/drivers?session_key=" + sessionKey, OpenF1Driver.class);
    }

    @Override
    public List<OpenF1SessionResult> fetchSessionResults(int sessionKey) {
        return fetchList("/session_result?session_key=" + sessionKey, OpenF1SessionResult.class);
    }

    private void rateLimit() {
        long now = System.currentTimeMillis();
        long elapsed = now - lastRequestTime;
        long minInterval = 600L;
        if (elapsed < minInterval) {
            try {
                sleep(minInterval - elapsed);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        lastRequestTime = System.currentTimeMillis();
    }

    private <T> List<T> fetchList(String path, Class<T> elementType) {
        int retries = 3;
        int baseDelay = 600;
        for (int attempt = 0; attempt <= retries; attempt++) {
            if (attempt > 0) {
                log.info("Retrying {} (attempt {}/{})", path, attempt, retries);
            }
            rateLimit();
            try {
                String json = restClient.get()
                        .uri(path)
                        .retrieve()
                        .body(String.class);
                if (json == null || json.isBlank() || json.equals("[]")) {
                    return Collections.emptyList();
                }
                return objectMapper.readValue(json, objectMapper.getTypeFactory()
                        .constructCollectionType(List.class, elementType));
            } catch (Exception e) {
                String msg = e.getMessage();
                boolean isRateLimit = msg != null && msg.contains("429");
                if (isRateLimit && attempt < retries) {
                    long backoff = (long) baseDelay * (1 << attempt);
                    log.warn("OpenF1 API rate limited on {}, retrying in {}ms", path, backoff);
                    try {
                        sleep(backoff);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                } else {
                    log.warn("OpenF1 API error fetching {}: {}", path, msg);
                    return Collections.emptyList();
                }
            }
        }
        return Collections.emptyList();
    }
}
