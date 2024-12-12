package com.tt.Together_time.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.tt.Together_time.domain.dto.KakaoUserInfo;
import com.tt.Together_time.domain.rdb.Member;
import com.tt.Together_time.repository.AuthRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class KakaoOAuth2UserService {
    @Value("${spring.security.oauth2.client.provider.kakao.user-info-uri}")
    private String KAKAO_USERINFO_REQUEST_URL;

    private final AuthRepository authRepository;

    public KakaoUserInfo getUserInfo(String kakaoToken) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + kakaoToken);
        headers.add("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");

        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
                KAKAO_USERINFO_REQUEST_URL, HttpMethod.GET, request, String.class);

        //System.out.println("카카오 API 응답: " + response.getBody());

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(response.getBody(), KakaoUserInfo.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("카카오 사용자 정보 파싱 실패", e);
        }
    }
}