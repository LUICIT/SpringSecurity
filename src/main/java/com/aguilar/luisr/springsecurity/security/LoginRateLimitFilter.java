package com.aguilar.luisr.springsecurity.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Component
public class LoginRateLimitFilter extends OncePerRequestFilter {

    private static final String LOGIN_PATH = "/api/v1/auth/login";

    private final Cache<String, Bucket> buckets = Caffeine.newBuilder()
            .expireAfterAccess(Duration.ofHours(1))
            .maximumSize(100_000)
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !("POST".equalsIgnoreCase(request.getMethod())
                && request.getRequestURI().equals(LOGIN_PATH));
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain chain)
            throws IOException, ServletException {

        ContentCachingRequestWrapper wrapped = new ContentCachingRequestWrapper(request, 1024);

        // Deja que el request se “cargue” antes de leer el body:
        // OJO: si intentas leer antes, puede estar vacío. Aquí leemos el body tal cual venga.
        String ip = extractClientIp(wrapped);
        String email = extractEmail(wrapped); // puede ser null si body inválido

        // Clave: ip + email (si no hay email, cae a ip)
        String ipKey = "login:ip:" + ip;
        String ipEmailKey = (email == null || email.isBlank())
                ? null
                : "login:ip_email:" + ip + ":" + email;

        // Bucket IP global (ej. 30/min)
        Bucket ipBucket = buckets.get(ipKey, k -> newIpBucket());

        // Bucket IP+email (ej. 5/min)
        Bucket ipEmailBucket = (ipEmailKey == null)
                ? null
                : buckets.get(ipEmailKey, k -> newIpEmailBucket());

        boolean okIp = ipBucket.tryConsume(1);
        boolean okIpEmail = (ipEmailBucket == null) || ipEmailBucket.tryConsume(1);

        if (!okIp || !okIpEmail) {
            response.setStatus(429);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"too_many_attempts\",\"message\":\"Demasiados intentos. Intenta más tarde.\"}");
            return;
        }

        chain.doFilter(wrapped, response);
    }

    private Bucket newIpBucket() {
        Refill refill = Refill.greedy(30, Duration.ofMinutes(1));
        Bandwidth limit = Bandwidth.classic(30, refill);
        return Bucket.builder().addLimit(limit).build();
    }

    private Bucket newIpEmailBucket() {
        Refill refill = Refill.greedy(5, Duration.ofMinutes(1));
        Bandwidth limit = Bandwidth.classic(5, refill);
        return Bucket.builder().addLimit(limit).build();
    }

    private String extractEmail(ContentCachingRequestWrapper request) {
        try {
            byte[] content = request.getContentAsByteArray();

            // Si aún no se cacheó, forzamos lectura leyendo el InputStream una vez
            if (content.length == 0) {
                content = request.getInputStream().readAllBytes();
            }

            if (content.length == 0) return null;

            String body = new String(content, StandardCharsets.UTF_8);
            JsonNode node = objectMapper.readTree(body);
            JsonNode emailNode = node.get("email");
            if (emailNode == null) return null;

            return emailNode.asText("").trim().toLowerCase();
        } catch (Exception e) {
            return null;
        }
    }

    private String extractClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

}
