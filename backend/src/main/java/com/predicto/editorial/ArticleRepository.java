package com.predicto.editorial;

import com.predicto.common.enums.Game;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ArticleRepository extends JpaRepository<Article, UUID> {

    Page<Article> findByStatusOrderByPublishedAtDesc(ArticleStatus status, Pageable pageable);

    Page<Article> findByStatusAndCategoryOrderByPublishedAtDesc(ArticleStatus status, ArticleCategory category, Pageable pageable);

    Page<Article> findByStatusAndGameOrderByPublishedAtDesc(ArticleStatus status, Game game, Pageable pageable);

    Page<Article> findByStatusAndCategoryAndGameOrderByPublishedAtDesc(ArticleStatus status, ArticleCategory category, Game game, Pageable pageable);

    Page<Article> findByStatusAndLanguageOrderByPublishedAtDesc(ArticleStatus status, String language, Pageable pageable);

    Page<Article> findByStatusAndCategoryAndLanguageOrderByPublishedAtDesc(ArticleStatus status, ArticleCategory category, String language, Pageable pageable);

    Page<Article> findByStatusAndGameAndLanguageOrderByPublishedAtDesc(ArticleStatus status, Game game, String language, Pageable pageable);

    Page<Article> findByStatusAndCategoryAndGameAndLanguageOrderByPublishedAtDesc(ArticleStatus status, ArticleCategory category, Game game, String language, Pageable pageable);

    Optional<Article> findBySlugAndStatus(String slug, ArticleStatus status);

    List<Article> findByFeaturedTrueAndStatusOrderByPublishedAtDesc(ArticleStatus status, Pageable pageable);

    List<Article> findByFeaturedTrueAndStatusAndLanguageOrderByPublishedAtDesc(ArticleStatus status, String language, Pageable pageable);

    boolean existsBySlug(String slug);
}
