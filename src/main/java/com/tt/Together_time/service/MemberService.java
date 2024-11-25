package com.tt.Together_time.service;

import com.tt.Together_time.domain.dto.KakaoUserInfo;
import com.tt.Together_time.domain.rdb.Member;
import com.tt.Together_time.repository.MemberRepository;
import com.tt.Together_time.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public String kakaoLogin(KakaoUserInfo kakaoUserInfo){
        String email = kakaoUserInfo.getKakao_account().getEmail();
        String nickname = kakaoUserInfo.getKakao_account().getProfile().getNickname();

        Member member = memberRepository.findByEmail(email)
                .orElseGet(()->{
                    Member newMember = Member.builder()
                            .email(email)
                            .nickname(nickname)
                            .build();
                    return memberRepository.save(newMember);
                });

        return jwtTokenProvider.generateToken(member.getEmail());
    }

    public Optional<Member> findByEmail(String email){
        return memberRepository.findByEmail(email);
    }

    public List<Member> findMember(String keyword) {
        return memberRepository.findMember(keyword);
    }

    public Optional<Member> findById(Long memberId) {
        return memberRepository.findById(memberId);
    }
}
