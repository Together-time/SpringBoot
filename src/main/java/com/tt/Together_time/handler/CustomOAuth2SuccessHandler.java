package com.tt.Together_time.handler;

import com.tt.Together_time.repository.RedisDao;
import com.tt.Together_time.security.JwtTokenProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CustomOAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisDao redisDao;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, IOException {
        DefaultOAuth2User oAuth2User = (DefaultOAuth2User) authentication.getPrincipal();
        Map<String, Object> kakaoAccount = (Map<String, Object>) oAuth2User.getAttribute("kakao_account");
        String email = kakaoAccount != null ? (String) kakaoAccount.get("email") : null;

        if (email == null) {
            throw new IllegalArgumentException("OAuth2 로그인 시 이메일 정보를 가져올 수 없습니다.");
        }

        String accessToken = jwtTokenProvider.generateToken(email);
        String refreshToken = jwtTokenProvider.generateRefreshToken(email);

        redisDao.setValues(email, refreshToken, Duration.ofDays(15));

        /*Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge((int) Duration.ofDays(15).getSeconds());
        response.addCookie(refreshTokenCookie);*/

        // JWT를 프론트엔드로 전달
        // HttpOnly & Secure 쿠키 사용으로 바꾸기 -> 회의 안건
        //response.sendRedirect("http://localhost:3000?token=" + accessToken);
        //response.sendRedirect("http://localhost:3000/auth/kakao/callback?token=" + accessToken);
    }
}