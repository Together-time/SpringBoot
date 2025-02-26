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
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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
                    // JWT 기반으로 DefaultOAuth2User 생성 - 일관되게
                    Map<String, Object> attributes = new HashMap<>();
                    attributes.put("email", email); // 최소한의 attributes 설정

                    DefaultOAuth2User oAuth2User = new DefaultOAuth2User(
                            Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                            attributes,
                            "email"); // getName()이 email을 반환하도록 설정

                    Authentication authentication = new UsernamePasswordAuthenticationToken(
                            oAuth2User, null, oAuth2User.getAuthorities()); // Principal을 DefaultOAuth2User로 설정
                    //Authentication authentication = new UsernamePasswordAuthenticationToken(email, null, Collections.emptyList());

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (ExpiredJwtException e) {
            throw new ExpiredJwtException(e.getHeader(), e.getClaims(), "Expired Token");
        } catch (JwtException | IllegalArgumentException e) {
            throw new JwtException("Invalid Token");
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
