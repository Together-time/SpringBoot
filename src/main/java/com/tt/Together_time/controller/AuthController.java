package com.tt.Together_time.controller;

import jakarta.servlet.http.HttpServletResponse;
import com.tt.Together_time.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final MemberService memberService;
    @Value("${spring.host.front}")
    private String frontURL;

    @PostMapping("/logout")
    public void logout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        memberService.logout(request, response);
        response.sendRedirect(frontURL);
    }

    @PostMapping("/refresh")    //새로운 access 토큰 발급
    public ResponseEntity<String> refreshAccessToken(HttpServletRequest request, HttpServletResponse response) {
         String newAccessToken = memberService.refreshAccessToken(request, response);
         return ResponseEntity.ok(newAccessToken);
    }

    @DeleteMapping
    public ResponseEntity<Boolean> withdraw(HttpServletRequest request, HttpServletResponse response){
        memberService.withdraw(request, response);
        return ResponseEntity.ok(true);
    }
}