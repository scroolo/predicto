package com.predicto.news;

import com.predicto.common.enums.Game;
import com.predicto.editorial.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.OffsetDateTime;
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
                String result = aiNewsService.generateArticle(item);
                String[] parts = result.split("\\|\\|\\|", 2);
                String title = parts.length > 1 ? parts[0].trim() : item.title();
                String content = parts.length > 1 ? parts[1].trim() : result;
                Article article = new Article();
                article.setTitle(title);
                article.setContent(content);
                article.setStatus(ArticleStatus.DRAFT);
                article.setGame(Game.valueOf(item.source().sport()));
                article.setSourceUrl(item.link());
                article.setCreatedAt(OffsetDateTime.now());

                // Required fields
                String slug = item.title().toLowerCase()
                    .replaceAll("[^a-z0-9\\s]", "")
                    .replaceAll("\\s+", "-")
                    .substring(0, Math.min(80, item.title().length()))
                    + "-" + System.currentTimeMillis();
                article.setSlug(slug);
                article.setSummary(content.substring(0, Math.min(200, content.length())));
                article.setCategory(ArticleCategory.NEWS);

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
