package com.tt.Together_time.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Slf4j
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {
    private final OnlineStatusWebSocketHandler webSocketHandler;
    private final ChatWebSocketHandler chatWebSocketHandler;

    @PostConstruct
    public void check() {
        log.info("✅ WebSocketConfig가 정상적으로 로드되었습니다!");
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        log.info("✅ WebSocket 핸들러 등록 시작");
        registry.addHandler(webSocketHandler, "/ws/online-status")
                .setAllowedOriginPatterns("http://localhost:3000")
                .withSockJS();

        registry.addHandler(chatWebSocketHandler, "/ws/chat")
                .setAllowedOriginPatterns("*")
                .withSockJS();
        log.info("✅ WebSocket 핸들러 등록 완료: /ws/chat");
    }
}
