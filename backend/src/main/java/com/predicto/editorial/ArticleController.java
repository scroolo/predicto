package com.predicto.editorial;

import com.predicto.common.enums.Game;
import com.predicto.editorial.dto.ArticleCardDto;
import com.predicto.editorial.dto.ArticleDetailDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    @GetMapping
    public ResponseEntity<Page<ArticleCardDto>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String game,
            @RequestParam(required = false) String language) {
        ArticleCategory cat = null;
        Game g = null;
        try { if (category != null) cat = ArticleCategory.valueOf(category.toUpperCase()); } catch (IllegalArgumentException e) {}
        try { if (game != null) g = Game.valueOf(game.toUpperCase()); } catch (IllegalArgumentException e) {}
        return ResponseEntity.ok(articleService.getPublishedArticles(PageRequest.of(page, size), cat, g, language));
    }

    @GetMapping("/featured")
    public ResponseEntity<List<ArticleCardDto>> featured(
            @RequestParam(required = false) String language) {
        return ResponseEntity.ok(articleService.getFeaturedArticles(language));
    }

    @GetMapping("/{slug}")
    public ResponseEntity<ArticleDetailDto> detail(@PathVariable String slug) {
        return articleService.getArticleBySlug(slug)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
