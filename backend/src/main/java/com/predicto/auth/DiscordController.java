package com.predicto.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/api/auth/discord")
@RequiredArgsConstructor
@Slf4j
public class DiscordController {

    private final DiscordAuthService discordAuthService;

    @Value("${discord.oauth2.client-id}")
    private String clientId;

    @Value("${discord.oauth2.client-secret}")
    private String clientSecret;

    @Value("${discord.oauth2.redirect-uri}")
    private String redirectUri;

    @Value("${discord.oauth2.scope}")
    private String scope;

    @Value("${discord.oauth2.frontend-url}")
    private String frontendUrl;

    @Value("${app.cookie.secure:false}")
    private boolean cookieSecure;

    @Value("${app.cookie.same-site:Lax}")
    private String cookieSameSite;

    @GetMapping
    public ResponseEntity<Map<String, String>> authorize() {
        String url = "https://discord.com/api/oauth2/authorize?" +
                "client_id=" + encode(clientId) +
                "&redirect_uri=" + encode(redirectUri) +
                "&response_type=code" +
                "&scope=" + encode(scope);
        return ResponseEntity.ok(Map.of("url", url));
    }

    @GetMapping("/callback")
    public ResponseEntity<Void> callback(@RequestParam("code") String code) {
        var tokenResponse = exchangeCode(code);
        String accessToken = tokenResponse.accessToken();
        var discordProfile = fetchProfile(accessToken);
        var result = discordAuthService.loginOrRegister(discordProfile);

        String token = result.token();
        log.info("Discord callback - redirecting with token");

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(frontendUrl + "/auth/callback?token=" + token))
                .build();
    }

    private TokenResponse exchangeCode(String code) {
        var client = RestClient.create();
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("grant_type", "authorization_code");
        body.add("code", code);
        body.add("redirect_uri", redirectUri);

        var response = client.post()
                .uri("https://discord.com/api/oauth2/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(body)
                .retrieve()
                .toEntity(Map.class);

        Map<String, Object> map = response.getBody();
        return new TokenResponse(
                (String) map.get("access_token"),
                (String) map.get("token_type"),
                (Integer) map.get("expires_in")
        );
    }

    private DiscordProfile fetchProfile(String accessToken) {
        var client = RestClient.create();
        var response = client.get()
                .uri("https://discord.com/api/users/@me")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .toEntity(Map.class);

        Map<String, Object> map = response.getBody();
        if (map == null) {
            throw new RuntimeException("Failed to fetch Discord profile");
        }
        return new DiscordProfile(
                (String) map.get("id"),
                (String) map.get("username"),
                (String) map.get("email"),
                (String) map.get("avatar")
        );
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private record TokenResponse(String accessToken, String tokenType, int expiresIn) {}
}
