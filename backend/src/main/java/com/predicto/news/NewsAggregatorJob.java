package com.predicto.news;

import com.predicto.editorial.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class NewsAggregatorJob {

    private final RssFeedService rssFeedService;
    private final AiNewsService aiNewsService;
    private final ArticleRepository articleRepository;
    private final Set<String> processedUrls = Collections.synchronizedSet(new HashSet<>());

    @Scheduled(fixedDelay = 3600000) // every hour
    public void run() {
        log.info("NewsAggregatorJob: starting...");
        List<RssItem> items = rssFeedService.fetchAll();
        log.info("NewsAggregatorJob: fetched {} RSS items", items.size());

        for (RssItem item : items) {
            if (processedUrls.contains(item.link())) continue;
            if (articleRepository.existsBySourceUrl(item.link())) continue;

            try {
                String content = aiNewsService.generateArticle(item);
                Article article = new Article();
                article.setTitle(item.title());
                article.setContent(content);
                article.setStatus(ArticleStatus.DRAFT);
                article.setGame(com.predicto.common.enums.Game.valueOf(item.source().sport()));
                article.setSourceUrl(item.link());
                article.setCreatedAt(java.time.OffsetDateTime.now());
                articleRepository.save(article);
                processedUrls.add(item.link());
                log.info("NewsAggregatorJob: created draft article: {}", item.title());
                Thread.sleep(2000); // rate limit
            } catch (Exception e) {
                log.error("NewsAggregatorJob: failed for {}: {}", item.title(), e.getMessage());
            }
        }
    }
}
