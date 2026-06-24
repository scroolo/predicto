package com.predicto.auth.security;

import java.util.UUID;

public record JwtUser(UUID id, String username, String role) {}
