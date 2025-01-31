package com.tt.Together_time.controller;

import com.tt.Together_time.domain.dto.KakaoUserInfo;
import com.tt.Together_time.domain.dto.MemberDto;
import jakarta.servlet.http.HttpServletResponse;
import com.tt.Together_time.service.KakaoUserService;
import com.tt.Together_time.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final MemberService memberService;
    private final KakaoUserService kakaoService; // 카카오 서비스: 토큰으로 사용자 정보 조회

    @GetMapping("/user")
    public ResponseEntity<String> getUserInfo() {
        String email = memberService.getUserEmail();
        return ResponseEntity.ok(email);
    }

    @PostMapping("/logout")
    public ResponseEntity<Boolean> logout(HttpServletRequest request, HttpServletResponse response){
        memberService.logout(request, response);
        return ResponseEntity.ok(true);
    }

    @GetMapping("/kakao/callback")
    public ResponseEntity<MemberDto> kakaoLogin(@RequestParam("code") String code, HttpServletResponse response) throws IOException {
        String accessToken = kakaoService.getAccessToken(code);
        KakaoUserInfo userInfo = kakaoService.getUserInfo(accessToken);
        MemberDto memberDto = memberService.kakaoLogin(userInfo, response);
        response.sendRedirect("http://localhost:3000?token=" + memberDto.getJwtToken());
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