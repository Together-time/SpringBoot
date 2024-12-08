package com.tt.Together_time.controller;

import com.tt.Together_time.domain.rdb.Member;
import com.tt.Together_time.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
    
    //현재 접속 중인 사용자
    //로그인 기준? -> 로그아웃을 안 했으면?
    //접속 판단 기준을 뭘로 하나
    //refresh token을 도입해서 장기간 재로그인을 필요로 하지 않는데 이거?
    //redis를 사용할 것 같고...
    //그런데 로그인이 기준이 아니면 뭐가 기준?
    //창이 켜진 거?? 이걸... 어케 알아?
    //그런데 카톡창 읽는.. 것도 비슷하지 않나
    //창이 켜진 게 기준일 듯? 카톡 읽음 표시 뜨는 거 생각해보면...
    //아 아닌가 카톡은 그냥 uri 요청 들어오는 순간 되는거잖아
}
