package com.predicto.auth.security;

import com.predicto.common.RateLimitFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final RateLimitFilter rateLimitFilter;

    @Value("${cors.allowed-origins}")
    private List<String> allowedOrigins;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(c -> c.configurationSource(corsSource()))
            .csrf(csrf -> csrf.disable())
            .headers(h -> h
                .contentTypeOptions(opt -> opt.disable())
                .addHeaderWriter((request, response) -> {
                    // response.setHeader("Content-Security-Policy",
                    //     "default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'; img-src 'self' https: data:; font-src 'self'; connect-src 'self' https://discord.com; frame-ancestors 'none'");
                    response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
                    response.setHeader("X-Frame-Options", "DENY");
                    response.setHeader("X-XSS-Protection", "1; mode=block");
                })
            )
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/assets/**").permitAll()
                .requestMatchers("/api/static/**").permitAll()
                .requestMatchers("/", "/index.html").permitAll()
                .requestMatchers("/*.js", "/*.css", "/*.ico", "/*.png", "/*.svg", "/*.webp", "/*.woff2", "/*.woff", "/*.ttf", "/*.map").permitAll()
                .requestMatchers("/actuator/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/register", "/api/auth/login").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/auth/me", "/api/auth/discord", "/api/auth/discord/callback").permitAll()
                .requestMatchers("/api/health").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/leagues", "/api/matches").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/matches/*/odds").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/seasons", "/api/seasons/active").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/seasons/*/leaderboard", "/api/seasons/*/rewards").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/articles", "/api/articles/featured", "/api/articles/*").permitAll()
                .requestMatchers("/api/teams/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/f1/**").permitAll()
                .requestMatchers("/api/f1/**").permitAll()
                .requestMatchers("/api/admin/articles/**").hasAnyRole("ADMIN", "EDITOR")
                .requestMatchers("/api/admin/articles").hasAnyRole("ADMIN", "EDITOR")
                .requestMatchers("/api/admin/users/**").hasRole("ADMIN")
                .requestMatchers("/api/admin/f1/**").hasRole("ADMIN")
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/matches/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/leagues/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/seasons/**").permitAll()
                .requestMatchers("/api/users/me/**").authenticated()
                .anyRequest().permitAll()
            )
            .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterAfter(jwtAuthFilter, RateLimitFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private CorsConfigurationSource corsSource() {
        var config = new CorsConfiguration();
        config.setAllowedOrigins(allowedOrigins);
        config.addAllowedOrigin("https://predicto-frontend-production.up.railway.app");
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
