package com.tt.Together_time.controller;

import jakarta.servlet.http.HttpServletResponse;
import com.tt.Together_time.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final MemberService memberService;

    /*@GetMapping("/user")
    public ResponseEntity<String> getUserInfo() {
        String email = memberService.getUserEmail();
        return ResponseEntity.ok(email);
    }
*/
    @PostMapping("/logout")
    public ResponseEntity<Boolean> logout(HttpServletRequest request, HttpServletResponse response){
        memberService.logout(request, response);
        return ResponseEntity.ok(true);
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