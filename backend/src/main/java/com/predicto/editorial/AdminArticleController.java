package com.predicto.editorial;

import com.predicto.auth.User;
import com.predicto.auth.UserRepository;
import com.predicto.auth.security.JwtUser;
import com.predicto.common.enums.Game;
import com.predicto.editorial.dto.CreateArticleRequest;
import com.predicto.editorial.dto.UpdateArticleRequest;
import jakarta.persistence.EntityManager;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/articles")
@RequiredArgsConstructor
@Slf4j
public class AdminArticleController {

    private final ArticleRepository articleRepository;
    private final ArticleService articleService;
    private final UserRepository userRepository;
    private final EntityManager entityManager;

    @GetMapping
    public ResponseEntity<Page<Article>> list(@RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(articleRepository.findAll(PageRequest.of(page, size, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt"))));
    }

    @GetMapping("/debug/article/{id}")
    @ResponseBody
    public String debugArticle(@PathVariable UUID id) {
        var articleRepo = articleRepository.findById(id);
        var articleEm = entityManager.find(Article.class, id);
        long count = articleRepository.count();
        return "findById=" + articleRepo.isPresent() + ", em.find=" + (articleEm != null) + ", total=" + count + ", id=" + id;
    }

    @GetMapping("/debug/article-sql/{id}")
    @ResponseBody
    public String debugSql(@PathVariable String id) {
        try {
            var result = entityManager.createNativeQuery(
                "SELECT id, title FROM articles WHERE CAST(id AS VARCHAR) = :id"
            ).setParameter("id", id).getResultList();
            return "SQL result count: " + result.size();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @GetMapping("/debug/article-type")
    @ResponseBody
    public String debugType() {
        try {
            var result = entityManager.createNativeQuery(
                "SELECT id::text, pg_typeof(id) FROM articles LIMIT 3"
            ).getResultList();
            StringBuilder sb = new StringBuilder();
            for (Object row : result) {
                Object[] cols = (Object[]) row;
                sb.append("id=").append(cols[0]).append(", type=").append(cols[1]).append("\n");
            }
            var schemaResult = entityManager.createNativeQuery(
                "SELECT column_name, data_type, udt_name FROM information_schema.columns WHERE table_name = 'articles' AND column_name = 'id'"
            ).getResultList();
            for (Object row : schemaResult) {
                Object[] cols = (Object[]) row;
                sb.append("column=").append(cols[0]).append(", data_type=").append(cols[1]).append(", udt_name=").append(cols[2]).append("\n");
            }
            sb.append("total=").append(result.size()).append(" rows\n");
            return sb.length() > 0 ? sb.toString() : "No articles found";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @GetMapping("/debug/article-jpql/{id}")
    @ResponseBody
    public String debugJpql(@PathVariable UUID id) {
        try {
            org.hibernate.Session session = entityManager.unwrap(org.hibernate.Session.class);
            var byId = session.byId(Article.class).load(id);

            var result = entityManager.createQuery(
                "SELECT a FROM Article a WHERE a.id = :id", Article.class
            ).setParameter("id", id).getResultList();

            var nativeResult = entityManager.createNativeQuery(
                "SELECT a.id, a.title FROM articles a WHERE a.id = ?"
            ).setParameter(1, id).getResultList();

            return "JPQL=" + result.size() + ", NATIVE(UUID param)=" + nativeResult.size()
                + ", byId=" + (byId != null) + ", id=" + id;
        } catch (Exception e) {
            return "Error: " + e.getClass().getSimpleName() + ": " + e.getMessage();
        }
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody CreateArticleRequest req,
                                    @AuthenticationPrincipal JwtUser jwtUser) {
        var author = userRepository.findById(jwtUser.id()).orElse(null);
        if (author == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        ArticleCategory category;
        Game game;
        try { category = ArticleCategory.valueOf(req.getCategory().toUpperCase()); }
        catch (IllegalArgumentException e) { return ResponseEntity.badRequest().body("Invalid category"); }
        try { game = Game.valueOf(req.getGame().toUpperCase()); }
        catch (IllegalArgumentException e) { return ResponseEntity.badRequest().body("Invalid game"); }

        String slug = articleService.generateSlug(req.getTitle());

        var article = Article.builder()
                .title(articleService.sanitize(req.getTitle()))
                .slug(slug)
                .summary(articleService.sanitize(req.getSummary()))
                .content(articleService.sanitize(req.getContent()))
                .coverImageUrl(req.getCoverImageUrl())
                .category(category)
                .game(game)
                .author(author)
                .language(req.getLanguage() != null ? req.getLanguage() : "sk")
                .featured(req.isFeatured())
                .build();

        article = articleRepository.save(article);
        return ResponseEntity.status(HttpStatus.CREATED).body(article);
    }

    private java.util.Optional<Article> findArticleById(UUID id) {
        var list = entityManager.createQuery(
            "SELECT a FROM Article a WHERE a.id = :id", Article.class
        ).setParameter("id", id).getResultList();
        return list.isEmpty() ? java.util.Optional.empty() : java.util.Optional.of(list.get(0));
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<?> update(@PathVariable UUID id, @Valid @RequestBody UpdateArticleRequest req) {
        log.info("PUT article: id={}", id);
        var articles = entityManager.createQuery(
            "SELECT a FROM Article a WHERE a.id = :id", Article.class
        ).setParameter("id", id).getResultList();
        log.info("Article found: {}", !articles.isEmpty());
        if (articles.isEmpty()) return ResponseEntity.notFound().build();
        var a = articles.get(0);

        if (req.getTitle() != null) a.setTitle(articleService.sanitize(req.getTitle()));
        if (req.getSummary() != null) a.setSummary(articleService.sanitize(req.getSummary()));
        if (req.getContent() != null) a.setContent(articleService.sanitize(req.getContent()));
        if (req.getCoverImageUrl() != null) a.setCoverImageUrl(req.getCoverImageUrl());
        if (req.getCategory() != null) {
            try { a.setCategory(ArticleCategory.valueOf(req.getCategory().toUpperCase())); }
            catch (IllegalArgumentException e) { return ResponseEntity.badRequest().body("Invalid category"); }
        }
        if (req.getGame() != null) {
            try { a.setGame(Game.valueOf(req.getGame().toUpperCase())); }
            catch (IllegalArgumentException e) { return ResponseEntity.badRequest().body("Invalid game"); }
        }
        if (req.getFeatured() != null) a.setFeatured(req.getFeatured());
        if (req.getLanguage() != null) a.setLanguage(req.getLanguage());

        return ResponseEntity.ok(a);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        if (findArticleById(id).isEmpty()) return ResponseEntity.notFound().build();
        entityManager.createQuery("DELETE FROM Article a WHERE a.id = :id")
            .setParameter("id", id).executeUpdate();
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/publish")
    @Transactional
    public ResponseEntity<?> publish(@PathVariable UUID id) {
        int updated = entityManager.createQuery(
            "UPDATE Article a SET a.status = :status, a.publishedAt = CASE WHEN a.publishedAt IS NULL THEN :now ELSE a.publishedAt END WHERE a.id = :id"
        ).setParameter("status", ArticleStatus.PUBLISHED)
         .setParameter("now", OffsetDateTime.now())
         .setParameter("id", id)
         .executeUpdate();
        if (updated == 0) return ResponseEntity.notFound().build();
        var articles = entityManager.createQuery(
            "SELECT a FROM Article a WHERE a.id = :id", Article.class
        ).setParameter("id", id).getResultList();
        return ResponseEntity.ok(articles.get(0));
    }

    @PatchMapping("/{id}/unpublish")
    @Transactional
    public ResponseEntity<?> unpublish(@PathVariable UUID id) {
        int updated = entityManager.createQuery(
            "UPDATE Article a SET a.status = :status WHERE a.id = :id"
        ).setParameter("status", ArticleStatus.DRAFT)
         .setParameter("id", id)
         .executeUpdate();
        if (updated == 0) return ResponseEntity.notFound().build();
        var articles = entityManager.createQuery(
            "SELECT a FROM Article a WHERE a.id = :id", Article.class
        ).setParameter("id", id).getResultList();
        return ResponseEntity.ok(articles.get(0));
    }

    @PatchMapping("/{id}/feature")
    @Transactional
    public ResponseEntity<?> toggleFeature(@PathVariable UUID id) {
        int updated = entityManager.createNativeQuery(
            "UPDATE articles SET featured = NOT featured WHERE id = ?"
        ).setParameter(1, id).executeUpdate();
        if (updated == 0) return ResponseEntity.notFound().build();
        var articles = entityManager.createQuery(
            "SELECT a FROM Article a WHERE a.id = :id", Article.class
        ).setParameter("id", id).getResultList();
        return ResponseEntity.ok(articles.get(0));
    }
}
