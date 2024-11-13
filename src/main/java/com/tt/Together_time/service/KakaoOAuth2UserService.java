package com.tt.Together_time.service;

import com.tt.Together_time.domain.rdb.Member;
import com.tt.Together_time.repository.AuthRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class KakaoOAuth2UserService {
    @Value("${spring.security.oauth2.client.provider.kakao.user-info-uri}")
    private String KAKAO_USERINFO_REQUEST_URL;
    private final AuthRepository authRepository;

    public Member getUserInfoFromKakao(String kakaoToken) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + kakaoToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    KAKAO_USERINFO_REQUEST_URL,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            String email = jsonNode.path("kakao_account").path("email").asText();
            String nickname = jsonNode.path("kakao_account").path("profile").path("nickname").asText();

            Optional<Member> memberOptional = authRepository.findByEmail(email);
            Member member = memberOptional.orElseGet(() -> {
                Member newMember = new Member(email, nickname);
                authRepository.save(newMember);
                return newMember;
            });

            return member;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}