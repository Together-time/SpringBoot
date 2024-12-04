package com.tt.Together_time.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Repository
@RequiredArgsConstructor
public class RedisDao {
    private final RedisTemplate<String, String> redisTemplate;

    // Refresh Token 저장 - 사용자 로그인
    public void setValues(String key, String value, Duration duration) {
        redisTemplate.opsForValue().set(key, value, duration);
    }

    // Refresh Token 조회 - 사용자 인증
    public String getValues(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    // Refresh Token 삭제 - 로그아웃
    public void deleteValues(String key) {
        redisTemplate.delete(key);
    }
}
