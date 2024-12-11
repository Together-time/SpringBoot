package com.tt.Together_time.service;

import com.tt.Together_time.domain.dto.KakaoUserInfo;
import com.tt.Together_time.domain.dto.MemberDto;
import com.tt.Together_time.domain.rdb.Member;
import com.tt.Together_time.exception.InvalidRefreshTokenException;
import com.tt.Together_time.repository.MemberRepository;
import com.tt.Together_time.repository.RedisDao;
import com.tt.Together_time.security.JwtAuthenticationFilter;
import com.tt.Together_time.security.JwtTokenProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RedisDao redisDao;
    /*
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
        //redisDao.setValues("MEMBER_ONLINE"+email, "logged");    //연결 상태 관리

        // Refresh Token을 HTTP-Only 쿠키에 저장
        Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
        refreshTokenCookie.setHttpOnly(true);  // JavaScript에서 접근하지 못하도록 설정
        refreshTokenCookie.setPath("/");       // 쿠키의 유효 경로 설정
        refreshTokenCookie.setMaxAge((int) Duration.ofDays(15).getSeconds()); // 쿠키 만료 시간 설정
        response.addCookie(refreshTokenCookie);

        return new MemberDto(
                member.getNickname(),
                member.getEmail(),
                accessToken,
                true
        );
    }
    */
    public Optional<Member> findByEmail(String email){
        return memberRepository.findByEmail(email);
    }

    public List<Member> findMember(String keyword) {
        return memberRepository.findMember(keyword);
    }

    public Optional<Member> findById(Long memberId) {
        return memberRepository.findById(memberId);
    }

    public void logout(HttpServletRequest request, HttpServletResponse response) {
        // 쿠키에서 Refresh Token 추출
        String refreshToken = Arrays.stream(request.getCookies())
                .filter(cookie -> "refreshToken".equals(cookie.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElse(null);
        if (refreshToken != null) {
            String email = jwtTokenProvider.getEmailFromToken(refreshToken);
            redisDao.deleteValues(email);
            //redisDao.deleteValues("MEMBER_ONLINE"+email);
        }
        // 클라이언트의 Refresh Token 쿠키 삭제
        Cookie cookie = new Cookie("refreshToken", null);
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0); // 쿠키 즉시 삭제
        cookie.setPath("/");
        response.addCookie(cookie);
        //블랙리스트를 사용하여 access token 무효화
        String accessToken = jwtAuthenticationFilter.resolveToken(request);
        Long expiration = jwtTokenProvider.getExpiration(accessToken);
        redisDao.addToBlacklist(accessToken, expiration);
    }

    //Access 토큰 재발급
    public String refreshAccessToken(HttpServletRequest request) {
        // 쿠키에서 Refresh Token 추출
        String refreshToken = Arrays.stream(request.getCookies())
                .filter(cookie -> "refreshToken".equals(cookie.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElseThrow(() -> new RuntimeException("Refresh Token not found"));

        // Refresh Token 검증 및 Access Token 발급
        String email = jwtTokenProvider.getEmailFromToken(refreshToken);
        String storedRefreshToken = redisDao.getValues(email);

        //System.out.println("refresh token : "+storedRefreshToken);

        if (storedRefreshToken != null && storedRefreshToken.equals(refreshToken)) {
            return jwtTokenProvider.generateToken(email);
        }
        throw new InvalidRefreshTokenException("Invalid Refresh Token");
    }

    public void withdraw(HttpServletRequest request) {
        String accessToken = jwtAuthenticationFilter.resolveToken(request);
        if(accessToken!=null){
            Long expiration = jwtTokenProvider.getExpiration(accessToken);
            redisDao.addToBlacklist(accessToken, expiration);
        }
        String refreshToken = Arrays.stream(request.getCookies())
                .filter(cookie -> "refreshToken".equals(cookie.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElse(null);
        if(refreshToken!=null){
            String email = jwtTokenProvider.getEmailFromToken(refreshToken);
            redisDao.deleteValues(email);
            memberRepository.deleteByEmail(email);
        }

        Cookie cookie = new Cookie("refreshToken", null);
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        request.setAttribute("exception", null);
    }
}
