package com.tt.Together_time.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
public class RedisDao {
    private final RedisTemplate<String, String> redisTemplate;

    //조회한 사용자인지
    public boolean isMember(String key, String value) {
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(key, value));
    }
    //조회 여부 저장
    public void addToSet(String key, String value) {
        redisTemplate.opsForSet().add(key, value);
    }
    //조회수 증가
    public Long increment(String key) {
        return redisTemplate.opsForValue().increment(key);
    }

    public void setValuesWithTTL(String key, String value, long ttlSeconds) {
        redisTemplate.opsForValue().set(key, value, ttlSeconds, TimeUnit.SECONDS);
    }

    public Set<String> getKeysByPattern(String pattern) {
        return redisTemplate.keys(pattern);
    }

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

    public void addToBlacklist(String token, long expiration){
        redisTemplate.opsForValue().set(token, "blacklisted", Duration.ofMillis(expiration));
    }
    public void publishMessage(String channel, String message) {
        redisTemplate.convertAndSend(channel, message);
    }
}
