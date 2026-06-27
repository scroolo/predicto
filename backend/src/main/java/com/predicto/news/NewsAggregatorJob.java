package com.predicto.news;

import com.predicto.common.enums.Game;
import com.predicto.editorial.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class NewsAggregatorJob {

    private final RssFeedService rssFeedService;
    private final AiNewsService aiNewsService;
    private final ArticleRepository articleRepository;
    private final Set<String> processedUrls = Collections.synchronizedSet(new HashSet<>());

    // @Scheduled(fixedDelay = 3600000)
    public void run() {
        processedUrls.clear();
        log.info("NewsAggregatorJob: starting...");

        LocalTime now = LocalTime.now(ZoneId.of("Europe/Bratislava"));
        int hour = now.getHour();

        if (hour < 7 || hour >= 22) {
            log.info("NewsAggregatorJob: outside active hours, skipping");
            return;
        }

        int maxPerRun = 2;
        int created = 0;

        List<RssItem> items = rssFeedService.fetchAll();
        log.info("NewsAggregatorJob: fetched {} RSS items", items.size());

        for (RssItem item : items) {
            if (created >= maxPerRun) break;
            if (processedUrls.contains(item.link())) continue;
            if (articleRepository.existsBySourceUrl(item.link())) {
                processedUrls.add(item.link());
                continue;
            }

            try {
                Article article = aiNewsService.generateArticle(item);
                if (article == null) continue;
                Article saved = articleRepository.save(article);
                log.info("NewsAggregatorJob: saved article id={}, title={}", saved.getId(), item.title());
                processedUrls.add(item.link());
                created++;
                Thread.sleep(2000);
            } catch (Exception e) {
                log.error("NewsAggregatorJob: error processing item {}: {}", item.link(), e.getMessage());
            }
        }

        log.info("NewsAggregatorJob: done, created {} articles this run", created);
    }
}
