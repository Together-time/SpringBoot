package com.tt.Together_time.service;

import com.tt.Together_time.domain.dto.KakaoUserInfo;
import com.tt.Together_time.domain.dto.MemberDto;
import com.tt.Together_time.domain.rdb.Member;
import com.tt.Together_time.repository.MemberRepository;
import com.tt.Together_time.repository.RedisDao;
import com.tt.Together_time.security.JwtTokenProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisDao redisDao;

    public MemberDto kakaoLogin(KakaoUserInfo kakaoUserInfo, HttpServletResponse response){
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
        String accessToken = jwtTokenProvider.generateToken(member.getEmail());   //JWT Access Token 발급
        String refreshToken = jwtTokenProvider.generateRefreshToken(member.getEmail());   //Refresh Token 발급

        redisDao.setValues(member.getEmail(), refreshToken, Duration.ofDays(15));
        // Refresh Token을 HTTP-Only 쿠키에 저장
        Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
        refreshTokenCookie.setHttpOnly(true);  // JavaScript에서 접근하지 못하도록 설정
        refreshTokenCookie.setPath("/");       // 쿠키의 유효 경로 설정
        refreshTokenCookie.setMaxAge((int) Duration.ofDays(15).getSeconds()); // 쿠키 만료 시간 설정
        response.addCookie(refreshTokenCookie);

        return new MemberDto(
                member.getNickname(),
                member.getEmail(),
                accessToken
        );
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

    public void logout(String email) {
        redisDao.deleteValues(email);
    }

    //Access 토큰 재발급
    public String refreshAccessToken(String refreshToken, String email) {
        String storedRefreshToken = redisDao.getValues(email);
        if (storedRefreshToken != null && storedRefreshToken.equals(refreshToken)) {
            return jwtTokenProvider.generateToken(email); // 새 Access Token 발급
        }
        throw new RuntimeException("Invalid Refresh Token");    //refresh token 유효x
    }

}
