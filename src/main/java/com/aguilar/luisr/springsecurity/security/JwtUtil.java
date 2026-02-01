package com.aguilar.luisr.springsecurity.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

@Component
public class JwtUtil {

    /**
     * Debe ser una clave en Base64 (>= 256 bits) para HS256.
     * Ejemplo: usa un secreto largo, luego conviértelo a Base64.
     */
    @Value("${security.authentication.jwt.jwt-key}")
    private String secretKey;
    private static final long EXPIRATION_MS = 24 * 60 * 60 * 1000L; // 24 horas

    /**
     * Genera token a partir del principal autenticado.
     * NO incluye password/hashes.
     */
    public String generateToken(UserDetails userDetails) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + EXPIRATION_MS);

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .claim("roles", roles)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Overload por compatibilidad: el segundo parámetro es un rol/tipo de usuario (NO password).
     * Si ya tienes `UserDetails`, usa `generateToken(UserDetails)`.
     */
    public String generateToken(String username, String userType) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + EXPIRATION_MS);

        return Jwts.builder()
                .setSubject(username)
                .claim("user_type", userType)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    // Extrae roles (si existen). Devuelve lista vacía si no existe el claim.
    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        try {
            return extractClaim(token, claims -> {
                Object value = claims.get("roles");
                if (value instanceof List<?> list) {
                    return (List<String>) list;
                }
                return List.of();
            });
        } catch (Exception e) {
            return List.of();
        }
    }

    // Validación básica: subject + expiración
    public boolean validateToken(String token, String username) {
        try {
            final String tokenUsername = extractUsername(token);
            return tokenUsername.equals(username) && !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    // Validación adicional que comprueba que user_type en el token coincide con el esperado
    public boolean validateToken(String token, String username, String expectedUserType) {
        if (!validateToken(token, username)) {
            return false;
        }
        String tokenUserType = extractUserType(token);
        return tokenUserType != null && tokenUserType.equalsIgnoreCase(expectedUserType);
    }

    // Extrae el username (subject)
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Extrae la expiración
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Extrae el claim "user_type" (puede devolver null si no existe)
    public String extractUserType(String token) {
        try {
            return extractClaim(token, claims -> claims.get("user_type", String.class));
        } catch (Exception e) {
            return null;
        }
    }

    // Genérico para extraer claims
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claimsResolver.apply(claims);
    }

    private boolean isTokenExpired(String token) {
        final Date expiration = extractExpiration(token);
        return expiration.before(new Date());
    }

    private Key getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

}
