package com.predicto.common;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    private static final Map<String, Bandwidth> LIMITS = Map.of(
        "/api/auth/login", Bandwidth.builder().capacity(5).refillIntervally(5, Duration.ofMinutes(1)).build(),
        "/api/auth/register", Bandwidth.builder().capacity(3).refillIntervally(3, Duration.ofMinutes(10)).build(),
        "/api/auth/discord/callback", Bandwidth.builder().capacity(10).refillIntervally(10, Duration.ofMinutes(1)).build()
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String path = request.getRequestURI();
        Bandwidth limit = LIMITS.get(path);
        if (limit == null) {
            chain.doFilter(request, response);
            return;
        }

        String ip = getClientIP(request);
        String key = ip + ":" + path;
        Bucket bucket = buckets.computeIfAbsent(key, k -> Bucket.builder().addLimit(limit).build());

        if (bucket.tryConsume(1)) {
            chain.doFilter(request, response);
        } else {
            response.setStatus(429);
            response.setHeader("Retry-After", "60");
            response.setContentType("application/json");
            response.getWriter().write("{\"message\":\"Too many requests. Please wait before trying again.\"}");
        }
    }

    private static String getClientIP(HttpServletRequest request) {
        String xf = request.getHeader("X-Forwarded-For");
        if (xf != null && !xf.isBlank()) {
            return xf.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
