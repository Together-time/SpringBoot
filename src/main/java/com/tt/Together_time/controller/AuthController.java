package com.tt.Together_time.controller;

import com.tt.Together_time.domain.dto.KakaoUserInfo;
import com.tt.Together_time.security.JwtTokenProvider;
import jakarta.servlet.http.HttpServletResponse;
import com.tt.Together_time.service.KakaoOAuth2UserService;
import com.tt.Together_time.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private final JwtTokenProvider jwtTokenProvider;
    private final MemberService memberService;
    private final KakaoOAuth2UserService kakaoService; // 카카오 서비스: 토큰으로 사용자 정보 조회

    @GetMapping("/user")
    public ResponseEntity<String> getUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getPrincipal().equals("anonymousUser")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String email = authentication.getName();

        return ResponseEntity.ok(email);
    }

    @PostMapping("/logout")
    public ResponseEntity<Boolean> logout(HttpServletRequest request, HttpServletResponse response){
        memberService.logout(request, response);
        return ResponseEntity.ok(true);
    }

    @GetMapping("/kakao/callback")
    public ResponseEntity<?> kakaoLogin(@AuthenticationPrincipal OAuth2User oAuth2User) {
        String kakaoAccessToken = oAuth2User.getAttribute("access_token");
        KakaoUserInfo userInfo = kakaoService.getUserInfo(kakaoAccessToken);
        //MemberDto memberDto = memberService.kakaoLogin(userInfo, response);
        String jwtToken = jwtTokenProvider.generateToken(userInfo.getEmail());
        return ResponseEntity.ok().body(
                "JWT Token: " + jwtToken + "\n" +
                        "User Info: " + userInfo.getNickname()
        );
    }

    @PostMapping("/refresh")    //새로운 access 토큰 발급
    public ResponseEntity<String> refreshAccessToken(HttpServletRequest request) {
         String newAccessToken = memberService.refreshAccessToken(request);
         return ResponseEntity.ok(newAccessToken);
    }

    @DeleteMapping
    public ResponseEntity<Boolean> withdraw(HttpServletRequest request){
        memberService.withdraw(request);
        return ResponseEntity.ok(true);
    }
}