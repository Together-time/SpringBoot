package com.tt.Together_time.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {
    private final OnlineStatusWebSocketHandler webSocketHandler;
    private final ChatWebSocketHandler chatWebSocketHandler;
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(webSocketHandler, "/ws/online-status")
                .setAllowedOrigins("http://localhost:3000")
                .withSockJS();

        registry.addHandler(chatWebSocketHandler, "/ws/chat")
                .setAllowedOrigins("http://localhost:3000")
                .withSockJS();
    }
}
