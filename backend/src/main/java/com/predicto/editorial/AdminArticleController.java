package com.predicto.editorial;

import com.predicto.auth.User;
import com.predicto.auth.UserRepository;
import com.predicto.auth.security.JwtUser;
import com.predicto.common.enums.Game;
import com.predicto.editorial.dto.CreateArticleRequest;
import com.predicto.editorial.dto.UpdateArticleRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    @GetMapping
    public ResponseEntity<Page<Article>> list(@RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(articleRepository.findAll(PageRequest.of(page, size, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt"))));
    }

    @GetMapping("/debug/article/{id}")
    @ResponseBody
    public String debugArticle(@PathVariable UUID id) {
        var article = articleRepository.findById(id);
        long count = articleRepository.count();
        return "found=" + article.isPresent() + ", total=" + count + ", id=" + id;
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

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable UUID id, @Valid @RequestBody UpdateArticleRequest req) {
        log.info("PUT article: id={}", id);
        var article = articleRepository.findById(id);
        log.info("Article found: {}", article.isPresent());
        if (article.isEmpty()) return ResponseEntity.notFound().build();
        var a = article.get();

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

        if (a.getStatus() == ArticleStatus.DRAFT && req.getFeatured() != null && req.getFeatured()) {
            // allow featuring even in draft
        }

        articleRepository.save(a);
        return ResponseEntity.ok(a);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        if (!articleRepository.existsById(id)) return ResponseEntity.notFound().build();
        articleRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/publish")
    public ResponseEntity<?> publish(@PathVariable UUID id) {
        var article = articleRepository.findById(id);
        if (article.isEmpty()) return ResponseEntity.notFound().build();
        var a = article.get();
        a.setStatus(ArticleStatus.PUBLISHED);
        if (a.getPublishedAt() == null) a.setPublishedAt(OffsetDateTime.now());
        articleRepository.save(a);
        return ResponseEntity.ok(a);
    }

    @PatchMapping("/{id}/unpublish")
    public ResponseEntity<?> unpublish(@PathVariable UUID id) {
        var article = articleRepository.findById(id);
        if (article.isEmpty()) return ResponseEntity.notFound().build();
        var a = article.get();
        a.setStatus(ArticleStatus.DRAFT);
        articleRepository.save(a);
        return ResponseEntity.ok(a);
    }

    @PatchMapping("/{id}/feature")
    public ResponseEntity<?> toggleFeature(@PathVariable UUID id) {
        var article = articleRepository.findById(id);
        if (article.isEmpty()) return ResponseEntity.notFound().build();
        var a = article.get();
        a.setFeatured(!a.getFeatured());
        articleRepository.save(a);
        return ResponseEntity.ok(a);
    }
}
