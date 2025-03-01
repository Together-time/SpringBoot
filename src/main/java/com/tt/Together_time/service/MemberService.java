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
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.time.Duration;

@Slf4j
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

    public void logout(HttpServletRequest request, HttpServletResponse response) {
        //리프레시 토큰
        String refreshToken = Arrays.stream(request.getCookies())
                .filter(cookie -> "refreshToken".equals(cookie.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElse(null);
        if (refreshToken != null) {
            String email = jwtTokenProvider.getEmailFromToken(refreshToken);
            redisDao.deleteValues(email);
        }

        //블랙리스트를 사용하여 access token 무효화
        String accessToken = jwtAuthenticationFilter.resolveToken(request);
        log.info("token {} {}", refreshToken, accessToken);
        Long expiration = jwtTokenProvider.getExpiration(accessToken);
        redisDao.addToBlacklist(accessToken, expiration);

        // 클라이언트의 쿠키 삭제
        deleteCookie(response);
    }

    @Transactional
    public void withdraw(HttpServletRequest request, HttpServletResponse response) {
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

        deleteCookie(response);
    }

    public String getUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getPrincipal().equals("anonymousUser")) {
            throw new AuthenticationCredentialsNotFoundException("로그인이 필요합니다.");
        }

        if (authentication instanceof OAuth2AuthenticationToken) {
            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
            Map<String, Object> attributes = oAuth2User.getAttributes();

            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
            String email = (String) kakaoAccount.get("email");

            if (email == null) {
                throw new AuthenticationCredentialsNotFoundException("이메일 정보를 가져올 수 없습니다.");
            }

            return email;
        }

        return authentication.getName();
    }

    private String getClientIp(HttpServletRequest request) {
        String[] headerTypes = {"X-Forwarded-For", "Proxy-Client-IP",
                "WL-Proxy-Client-IP", "HTTP_CLIENT_IP", "HTTP_X_FORWARDED_FOR"};

        String ip = "";
        for(String headerType: headerTypes) {
            ip = request.getHeader(headerType);
            if(ip != null) break;
        }

        if(ip==null) ip = request.getRemoteAddr();
        return ip;
    }

    private void deleteCookie(HttpServletResponse response){
        String[] tokensInCookie = {"refreshToken", "accessToken"};
        for(int i=0;i<tokensInCookie.length;i++){
            Cookie cookie = new Cookie(tokensInCookie[i], null);
            cookie.setHttpOnly(true);
            cookie.setSecure(true);
            cookie.setMaxAge(0);
            cookie.setPath("/");
            response.addCookie(cookie);
        }
    }
}