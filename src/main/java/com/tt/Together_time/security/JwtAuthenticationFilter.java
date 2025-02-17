package com.tt.Together_time.security;

import com.tt.Together_time.exception.BlacklistedTokenException;
import com.tt.Together_time.repository.RedisDao;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
//Authorization 헤더를 추출해 JWT 토큰 추출+유효성 검증+SecurityContext에 인증 정보 저장
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisDao redisDao;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = resolveToken(request);

        try {
            if (token != null) {
                if (redisDao.getValues(token) != null)
                    throw new BlacklistedTokenException("Token is blacklisted. Please login again.");

                if (jwtTokenProvider.validateToken(token)) {
                    String email = jwtTokenProvider.getEmailFromToken(token);
                    Authentication authentication = new UsernamePasswordAuthenticationToken(
                            email, null, Collections.emptyList());
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (ExpiredJwtException e) {
            // Access Token이 만료된 경우 예외 처리
            request.setAttribute("exception", "ExpiredToken");
        } catch (JwtException | IllegalArgumentException e) {
            // 토큰이 조작된 경우 예외 처리
            request.setAttribute("exception", "InvalidToken");
        }
        filterChain.doFilter(request, response);
    }
    public String resolveToken(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("accessToken".equals(cookie.getName()))
                    return cookie.getValue();
            }
        }
        return null;
    }
}
