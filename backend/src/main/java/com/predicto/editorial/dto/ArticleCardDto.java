package com.predicto.editorial.dto;

import com.predicto.common.enums.Game;
import com.predicto.editorial.ArticleCategory;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ArticleCardDto(
    UUID id,
    String title,
    String slug,
    String summary,
    String coverImageUrl,
    ArticleCategory category,
    Game game,
    String language,
    String authorDisplayName,
    OffsetDateTime publishedAt,
    boolean featured
) {}
