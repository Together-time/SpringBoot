package com.tt.Together_time.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class KakaoController {

    @GetMapping("/kakao-callback")
    public ResponseEntity<?> handleKakaoCallback(@RequestParam String code) {
        // Authorization Code 처리
        System.out.println("Authorization Code: " + code);
        return ResponseEntity.ok("Kakao Login Success");
    }
}
