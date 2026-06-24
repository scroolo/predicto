package com.predicto.editorial.dto;

import com.predicto.common.enums.Game;
import com.predicto.editorial.ArticleCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateArticleRequest {
    @NotBlank @Size(max = 255)
    private String title;
    @NotBlank @Size(max = 500)
    private String summary;
    @NotBlank
    private String content;
    private String coverImageUrl;
    @NotBlank
    private String category;
    @NotBlank
    private String game;
    @Pattern(regexp = "sk|en", message = "Language must be 'sk' or 'en'")
    private String language;
    private boolean featured;
}
