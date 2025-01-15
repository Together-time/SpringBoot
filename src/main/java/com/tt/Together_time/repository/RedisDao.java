package com.tt.Together_time.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class RedisDao {
    private final RedisTemplate<String, String> redisTemplate;

    public void setValues(String key, String data) {
        ValueOperations<String, String> values = redisTemplate.opsForValue();
        values.set(key, data);
    }

    public void setValuesList(String key, String data) {
        redisTemplate.opsForList().rightPushAll(key,data);
    }

    public List<String> getValuesList(String key) {
        Long len = redisTemplate.opsForList().size(key);
        return len == 0 ? new ArrayList<>() : redisTemplate.opsForList().range(key, 0, len-1);
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
