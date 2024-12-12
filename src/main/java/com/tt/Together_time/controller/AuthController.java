package com.tt.Together_time.controller;

import com.tt.Together_time.domain.dto.KakaoUserInfo;
import com.tt.Together_time.domain.dto.MemberDto;
import jakarta.servlet.http.HttpServletResponse;
import com.tt.Together_time.service.KakaoOAuth2UserService;
import com.tt.Together_time.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

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

    @PostMapping("/kakao")
    public ResponseEntity<MemberDto> kakaoLogin(@RequestParam("accessToken") String accessToken, HttpServletResponse response) {
        KakaoUserInfo userInfo = kakaoService.getUserInfo(accessToken);
        MemberDto memberDto = memberService.kakaoLogin(userInfo, response);
        return ResponseEntity.ok(memberDto);
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