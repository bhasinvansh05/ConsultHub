package com.consultingplatform.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import io.jsonwebtoken.Claims;

@Component
public class JwtTokenUtil {

    private final String jwtSecret;
    private final long jwtExpirationMs;
    private final String jwtPrefix;

    public JwtTokenUtil(@Value("${security.jwt.secret}") String jwtSecret,
                        @Value("${security.jwt.expiration-ms:3600000}") long jwtExpirationMs,
                        @Value("${security.jwt.prefix:Bearer}") String jwtPrefix) {
        this.jwtSecret = jwtSecret == null ? "changeit" : jwtSecret;
        this.jwtExpirationMs = jwtExpirationMs;
        this.jwtPrefix = jwtPrefix == null ? "Bearer" : jwtPrefix;
    }

    private Key getSigningKey() {
        try {
            byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
            // JJWT requires a key of sufficient length for HS256 (256 bits / 32 bytes).
            // If the provided secret is shorter, derive a 32-byte key using SHA-256.
            if (keyBytes.length < 32) {
                MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
                keyBytes = Arrays.copyOf(sha256.digest(keyBytes), 32);
            }
            return Keys.hmacShaKeyFor(keyBytes);
        } catch (Exception ex) {
            // As a last-resort fallback, use the raw bytes (may still throw),
            // but avoid failing bean creation with an unchecked exception here.
            byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
            return Keys.hmacShaKeyFor(Arrays.copyOf(keyBytes, Math.max(32, keyBytes.length)));
        }
    }

    public String generateToken(UserDetails userDetails) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + jwtExpirationMs);
        Claims claims = Jwts.claims().setSubject(userDetails.getUsername());
        // include authorities as a simple list of strings
        List<String> roles = userDetails.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toList());
        claims.put("roles", roles);

        // include userId when available (CustomUserDetails exposes it)
        if (userDetails instanceof com.consultingplatform.security.CustomUserDetails) {
            Long id = ((com.consultingplatform.security.CustomUserDetails) userDetails).getId();
            if (id != null) claims.put("userId", id);
        }

        return Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(now)
            .setExpiration(exp)
            .signWith(getSigningKey(), SignatureAlgorithm.HS256)
            .compact();
    }

        public Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(getSigningKey())
            .build()
            .parseClaimsJws(token)
            .getBody();
        }

    public String extractUsername(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        try {
            String username = extractUsername(token);
            return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        } catch (Exception ex) {
            return false;
        }
    }

    public boolean isTokenExpired(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        Date expiration = claims.getExpiration();
        return expiration.before(new Date());
    }

    public String stripPrefix(String header) {
        if (header == null) return null;
        String p = jwtPrefix.trim();
        if (header.startsWith(p + " ")) {
            return header.substring(p.length()).trim();
        }
        return header;
    }
}
