package com.predicto.editorial.dto;

import com.predicto.common.enums.Game;
import com.predicto.editorial.ArticleCategory;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateArticleRequest {
    @Size(max = 255)
    private String title;
    @Size(max = 500)
    private String summary;
    private String content;
    private String coverImageUrl;
    private String category;
    private String game;
    @Pattern(regexp = "sk|en", message = "Language must be 'sk' or 'en'")
    private String language;
    private Boolean featured;
}
