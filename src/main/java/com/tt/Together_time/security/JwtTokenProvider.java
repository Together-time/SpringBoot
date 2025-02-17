package com.tt.Together_time.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

//JWT 생성 및 검증 클래스
@Component
public class JwtTokenProvider { //JWT의 생성 및 검증 역할 수행 Utility
    @Value("${spring.jwt.secret}")
    private String secretKey;

    private final long validityInMilliseconds = 30*60*1000L;    //30분
    //리프레시 토큰의 만료 시간을 Redis TTL보다 짧게 설정해 만료된 토큰으로 Redis를 조회하는 경우가 없도록 할 것
    private final long refreshValidityInMilliseconds = 14 * 24 * 60 * 60 * 1000L; // 14일

    public String generateToken(String email) {
        Claims claims = Jwts.claims().setSubject(email);

        Date now = new Date();
        Date validity = new Date(now.getTime()+validityInMilliseconds);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    public String getEmailFromToken(String token) {
        return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody().getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String generateRefreshToken(String email) {
        Claims claims = Jwts.claims().setSubject(email);
        Date now = new Date();
        Date validity = new Date(now.getTime() + refreshValidityInMilliseconds);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)    //밀리세컨드로 저장해야 하므로 시간 변환 필수
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    //access token 남은 유효시간
    public Long getExpiration(String accessToken){
        Date expiration = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(accessToken)
                .getBody()
                .getExpiration();
        Long now = new Date().getTime();
        return (expiration.getTime()-now);
    }
}