package com.tt.Together_time.controller;

import com.tt.Together_time.domain.rdb.Member;
import com.tt.Together_time.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/member")
public class MemberController {
    private final MemberService memberService;
    //멤버 검색 - 팀원 추가
    @GetMapping
    public ResponseEntity<List<Member>> findMember(@RequestParam String keyword){
        List<Member> members = memberService.findMember(keyword);
        return ResponseEntity.ok(members);
    }

    @GetMapping("/user")
    public ResponseEntity<Map<String, String>> getUserInfo() {
        String email = memberService.getUserEmail();
        Member member = memberService.findByEmail(email);

        Map<String, String> userInfoMap = new HashMap<>();
        userInfoMap.put("email", email);
        userInfoMap.put("nickname", member.getNickname());
        return ResponseEntity.ok(userInfoMap);
    }
}