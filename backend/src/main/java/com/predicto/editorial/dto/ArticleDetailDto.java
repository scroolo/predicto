package com.predicto.editorial.dto;

import com.predicto.common.enums.Game;
import com.predicto.editorial.ArticleCategory;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ArticleDetailDto(
    UUID id,
    String title,
    String slug,
    String summary,
    String content,
    String coverImageUrl,
    ArticleCategory category,
    Game game,
    String language,
    String authorDisplayName,
    String authorAvatarUrl,
    OffsetDateTime publishedAt,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {}
