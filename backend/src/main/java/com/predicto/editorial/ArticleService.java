package com.predicto.editorial;

import com.predicto.editorial.dto.ArticleCardDto;
import com.predicto.editorial.dto.ArticleDetailDto;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ArticleService {

    private final ArticleRepository articleRepository;

    public String sanitize(String input) {
        if (input == null) return null;
        return Jsoup.clean(input, Safelist.none());
    }

    public String generateSlug(String title) {
        String slug = title.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
        if (slug.isEmpty()) slug = "article";
        String base = slug;
        int suffix = 2;
        while (articleRepository.existsBySlug(slug)) {
            slug = base + "-" + suffix++;
        }
        return slug;
    }

    public Page<ArticleCardDto> getPublishedArticles(PageRequest pageable, ArticleCategory category, com.predicto.common.enums.Game game, String language) {
        Page<Article> articles;
        if (category != null && game != null && language != null) {
            articles = articleRepository.findByStatusAndCategoryAndGameAndLanguageOrderByPublishedAtDesc(ArticleStatus.PUBLISHED, category, game, language, pageable);
        } else if (category != null && game != null) {
            articles = articleRepository.findByStatusAndCategoryAndGameOrderByPublishedAtDesc(ArticleStatus.PUBLISHED, category, game, pageable);
        } else if (category != null && language != null) {
            articles = articleRepository.findByStatusAndCategoryAndLanguageOrderByPublishedAtDesc(ArticleStatus.PUBLISHED, category, language, pageable);
        } else if (game != null && language != null) {
            articles = articleRepository.findByStatusAndGameAndLanguageOrderByPublishedAtDesc(ArticleStatus.PUBLISHED, game, language, pageable);
        } else if (language != null) {
            articles = articleRepository.findByStatusAndLanguageOrderByPublishedAtDesc(ArticleStatus.PUBLISHED, language, pageable);
        } else if (category != null) {
            articles = articleRepository.findByStatusAndCategoryOrderByPublishedAtDesc(ArticleStatus.PUBLISHED, category, pageable);
        } else if (game != null) {
            articles = articleRepository.findByStatusAndGameOrderByPublishedAtDesc(ArticleStatus.PUBLISHED, game, pageable);
        } else {
            articles = articleRepository.findByStatusOrderByPublishedAtDesc(ArticleStatus.PUBLISHED, pageable);
        }
        return articles.map(a -> new ArticleCardDto(
                a.getId(), a.getTitle(), a.getSlug(), a.getSummary(),
                a.getCoverImageUrl(), a.getCategory(), a.getGame(), a.getLanguage(),
                (a.getAuthor() != null ? a.getAuthor().getDisplayName() : "Predicto AI"), a.getPublishedAt(), a.getFeatured()));
    }

    public List<ArticleCardDto> getFeaturedArticles(String language) {
        List<Article> articles;
        if (language != null) {
            articles = articleRepository.findByFeaturedTrueAndStatusAndLanguageOrderByPublishedAtDesc(
                    ArticleStatus.PUBLISHED, language, PageRequest.of(0, 3));
        } else {
            articles = articleRepository.findByFeaturedTrueAndStatusOrderByPublishedAtDesc(
                    ArticleStatus.PUBLISHED, PageRequest.of(0, 3));
        }
        return articles.stream()
                .map(a -> new ArticleCardDto(
                        a.getId(), a.getTitle(), a.getSlug(), a.getSummary(),
                        a.getCoverImageUrl(), a.getCategory(), a.getGame(), a.getLanguage(),
                        (a.getAuthor() != null ? a.getAuthor().getDisplayName() : "Predicto AI"), a.getPublishedAt(), a.getFeatured()))
                .toList();
    }

    public Optional<ArticleDetailDto> getArticleBySlug(String slug) {
        return articleRepository.findBySlugAndStatus(slug, ArticleStatus.PUBLISHED)
                .map(a -> new ArticleDetailDto(
                        a.getId(), a.getTitle(), a.getSlug(), a.getSummary(), a.getContent(),
                        a.getCoverImageUrl(), a.getCategory(), a.getGame(), a.getLanguage(),
                        (a.getAuthor() != null ? a.getAuthor().getDisplayName() : "Predicto AI"), (a.getAuthor() != null ? a.getAuthor().getAvatarUrl() : null),
                        a.getPublishedAt(), a.getCreatedAt(), a.getUpdatedAt()));
    }
}
