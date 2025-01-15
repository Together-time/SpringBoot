package com.tt.Together_time.config;

import com.tt.Together_time.service.RedisMessageSubscriber;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        return redisTemplate;
    }
    
    //redis 연결 설정 및 pub/sub 채널 구독 초기화
    @Bean   //redis pub//sub 구독을 위한 리스너 컨테이너 - 특정 redis 채널에 대해 구독 및 처리
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            MessageListenerAdapter messageListenerAdapter) {

        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);  //redis와 연결 생성하는 팩토리
        container.addMessageListener(messageListenerAdapter, new ChannelTopic("chat:project:*")); // Redis Pub/Sub 채널 구독
        return container;
    }

    @Bean
    public MessageListenerAdapter messageListenerAdapter(RedisMessageSubscriber subscriber){
        return new MessageListenerAdapter(subscriber);
    }

    @Bean   //pub/sub 채널 정의
    public ChannelTopic topic(){
        return new ChannelTopic("chat:project:");
    }
}
