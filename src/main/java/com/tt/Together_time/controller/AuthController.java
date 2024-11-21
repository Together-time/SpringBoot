package com.tt.Together_time.controller;

import com.tt.Together_time.domain.dto.KakaoUserInfo;
import com.tt.Together_time.domain.dto.MemberDto;
import com.tt.Together_time.domain.rdb.Member;
import com.tt.Together_time.service.KakaoOAuth2UserService;
import com.tt.Together_time.service.MemberService;
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
    public ResponseEntity<MemberDto> getUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getPrincipal().equals("anonymousUser")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String email = authentication.getName();
        Member member = memberService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        return ResponseEntity.ok().body(new MemberDto(member.getNickname(), member.getEmail()));
    }

    @PostMapping("/kakao")
    public ResponseEntity<?> kakaoLogin(@RequestParam("accessToken") String accessToken) {
        KakaoUserInfo userInfo = kakaoService.getUserInfo(accessToken);
        String jwtToken = memberService.kakaoLogin(userInfo);
        return ResponseEntity.ok(jwtToken);
    }
}