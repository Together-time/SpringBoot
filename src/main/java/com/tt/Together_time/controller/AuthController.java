package com.tt.Together_time.controller;

import com.tt.Together_time.domain.dto.AuthResponse;
import com.tt.Together_time.domain.rdb.Member;
import com.tt.Together_time.security.JwtUtil;
import com.tt.Together_time.service.KakaoOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final JwtUtil jwtUtil;
    private final KakaoOAuth2UserService kakaoService; // 카카오 서비스: 토큰으로 사용자 정보 조회

    @GetMapping("/user")
    public String getUserInfo(Authentication authentication) {
        Authentication aa= SecurityContextHolder.getContext().getAuthentication();
        String userName = aa.getName(); // 사용자 이름 또는 ID 가져오기
        Object principal = aa.getPrincipal(); // UserDetails 객체 또는 사용자 정보

        System.out.println("인증 정보 : "+userName+" "+principal.toString());
        return "Hello, " + authentication.getName(); // 인증된 사용자 이름 반환
    }

    @PostMapping("/kakao")
    public ResponseEntity<?> kakaoLogin(@RequestBody String kakaoToken) {
        Member member = kakaoService.getUserInfoFromKakao(kakaoToken);

        if (member != null) {
            String jwt = jwtUtil.generateToken(member.getEmail());
            return ResponseEntity.ok().body(new AuthResponse(jwt));
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}
