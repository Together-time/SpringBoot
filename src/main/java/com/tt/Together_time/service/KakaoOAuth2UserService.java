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
public class KakaoOAuth2UserService {
    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String CLIENT_ID;
    @Value("${spring.security.oauth2.client.provider.kakao.token-uri}")
    private String TOKEN_REQUEST_URL;
    @Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
    private String REDIRECT_URI;
    @Value("${spring.security.oauth2.client.provider.kakao.user-info-uri}")
    private String USER_INFO_REQUEST_URL;

    public KakaoUserInfo getUserInfo(String kakaoToken) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + kakaoToken);
        headers.add("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");

        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
                USER_INFO_REQUEST_URL, HttpMethod.GET, request, String.class);

        //System.out.println("카카오 API 응답: " + response.getBody());

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(response.getBody(), KakaoUserInfo.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("카카오 사용자 정보 파싱 실패", e);
        }
    }

    public String getAccessToken(String code) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", CLIENT_ID);
        params.add("redirect_uri", REDIRECT_URI);
        params.add("code", code);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<String> response = restTemplate.exchange(
                TOKEN_REQUEST_URL, HttpMethod.POST, request, String.class
        );

        // 액세스 토큰 파싱
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            return jsonNode.get("access_token").asText();
        } catch (JsonProcessingException e) {
            throw new RuntimeException("액세스 토큰 파싱 실패", e);
        }
    }
}