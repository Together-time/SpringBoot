package com.tt.Together_time.handler;

import com.tt.Together_time.repository.RedisDao;
import com.tt.Together_time.security.JwtTokenProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomOAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisDao redisDao;

    @Value("${spring.host.front}")
    private String frontURL;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        DefaultOAuth2User oAuth2User = (DefaultOAuth2User) authentication.getPrincipal();
        Map<String, Object> kakaoAccount = (Map<String, Object>) oAuth2User.getAttribute("kakao_account");
        String email = kakaoAccount != null ? (String) kakaoAccount.get("email") : null;

        if (email == null) {
            throw new IllegalArgumentException("OAuth2 로그인 시 이메일 정보를 가져올 수 없습니다.");
        }

        String accessToken = jwtTokenProvider. generateToken(email);
        String refreshToken = jwtTokenProvider.generateRefreshToken(email);
        storeRefreshToken(email, refreshToken, request);

        Cookie accessTokenCookie = new Cookie("accessToken", accessToken);
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setSecure(true);
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge((int) Duration.ofMinutes(30).getSeconds());


        Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge((int) Duration.ofDays(15).getSeconds());

        response.addCookie(accessTokenCookie);
        response.addCookie(refreshTokenCookie);

        response.sendRedirect(frontURL);
    }

     private void storeRefreshToken(String email, String refreshToken, HttpServletRequest request) {
        String clientIp = getClientIp(request);
        String userAgent = request.getHeader("User-Agent");

        String tokenData = refreshToken + "|" + clientIp + "|" + userAgent;
        redisDao.setValues(email, tokenData, Duration.ofDays(15));
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
}