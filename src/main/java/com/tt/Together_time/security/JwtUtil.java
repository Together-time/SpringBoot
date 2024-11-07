package com.tt.Together_time.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

//JWT 생성 및 검증 클래스
@Component
public class JwtUtil {
    @Value("${jwt.secret}")  // application.yml의 jwt.secret 값 주입
    private String secretKey;

    public String generateToken(String userId) {
        return Jwts.builder()
                .setSubject(userId)
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    public Claims extractClaims(String token) {
        return Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean isTokenValid(String token, String userId) {
        String extractedUserId = extractClaims(token).getSubject();
        return (userId.equals(extractedUserId));
    }
}