package com.predicto.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RoleUpdateRequest {
    @NotBlank
    private String role;
}
