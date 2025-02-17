package com.tt.Together_time.service;

import com.tt.Together_time.domain.rdb.Member;
import com.tt.Together_time.exception.InvalidRefreshTokenException;
import com.tt.Together_time.repository.MemberRepository;
import com.tt.Together_time.repository.RedisDao;
import com.tt.Together_time.security.JwtAuthenticationFilter;
import com.tt.Together_time.security.JwtTokenProvider;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RedisDao redisDao;

    public Member findByEmail(String email){
        return memberRepository.findByEmail(email).orElseThrow(()-> new EntityNotFoundException("존재하지 않는 멤버입니다."));
    }

    public List<Member> findMember(String keyword) {
        return memberRepository.findMember(keyword);
    }

    public void logout(HttpServletRequest request, HttpServletResponse response) {
        // 쿠키에서 Refresh Token 추출
        String refreshToken = Arrays.stream(request.getCookies())
                .filter(cookie -> "refreshToken".equals(cookie.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElse(null);
        if (refreshToken != null) {
            String email = jwtTokenProvider.getEmailFromToken(refreshToken);
            redisDao.deleteValues(email);
        }
        // 클라이언트의 Refresh Token 쿠키 삭제
        Cookie cookie = new Cookie("refreshToken", null);
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0); // 쿠키 즉시 삭제
        cookie.setPath("/");
        response.addCookie(cookie);
        //블랙리스트를 사용하여 access token 무효화
        String accessToken = jwtAuthenticationFilter.resolveToken(request);
        Long expiration = jwtTokenProvider.getExpiration(accessToken);
        redisDao.addToBlacklist(accessToken, expiration);
    }

    //Access 토큰 재발급(리프레시 토큰도 같이 재발급-탈취 방지)
    public String refreshAccessToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = Arrays.stream(request.getCookies())
                .filter(cookie -> "refreshToken".equals(cookie.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElseThrow(() -> new RuntimeException("Refresh Token not found"));
        String email = jwtTokenProvider.getEmailFromToken(refreshToken);
        String storedValue = redisDao.getValues(email);

        if (storedValue == null)
            throw new InvalidRefreshTokenException("Refresh Token not found in Redis.");

        String[] tokenData = storedValue.split("|");
        if (tokenData.length < 3) {
            throw new InvalidRefreshTokenException("Stored token format is invalid.");
        }

        String storedRefreshToken = tokenData[0];
        String storedIp = tokenData[1];
        String storedUserAgent = tokenData[2];

        String clientIp = getClientIp(request);
        String userAgent = request.getHeader("User-Agent");

        if (!refreshToken.equals(storedRefreshToken) || !storedIp.equals(clientIp) || !storedUserAgent.equals(userAgent)) {
            throw new InvalidRefreshTokenException("Token mismatch or unauthorized client.");
        }

        redisDao.deleteValues(email);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(email);
        redisDao.setValues(email, newRefreshToken, Duration.ofDays(15));

        Cookie newRefreshTokenCookie = new Cookie("refreshToken", newRefreshToken);
        newRefreshTokenCookie.setHttpOnly(true);
        newRefreshTokenCookie.setPath("/");
        newRefreshTokenCookie.setMaxAge((int) Duration.ofDays(15).getSeconds());
        response.addCookie(newRefreshTokenCookie);

        return jwtTokenProvider.generateToken(email);
    }

    public void withdraw(HttpServletRequest request) {
        String accessToken = jwtAuthenticationFilter.resolveToken(request);
        if(accessToken!=null){
            Long expiration = jwtTokenProvider.getExpiration(accessToken);
            redisDao.addToBlacklist(accessToken, expiration);
        }
        String refreshToken = Arrays.stream(request.getCookies())
                .filter(cookie -> "refreshToken".equals(cookie.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElse(null);
        if(refreshToken!=null){
            String email = jwtTokenProvider.getEmailFromToken(refreshToken);
            redisDao.deleteValues(email);
            memberRepository.deleteByEmail(email);
        }

        Cookie cookie = new Cookie("refreshToken", null);
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        request.setAttribute("exception", null);
    }

    public String getUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getPrincipal().equals("anonymousUser")) {
            throw new AuthenticationCredentialsNotFoundException("로그인이 필요합니다.");
        }

        return authentication.getName();
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            return ip.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
