package com.tt.Together_time.config;

import com.tt.Together_time.websocket.ChatWebSocketHandler;
import com.tt.Together_time.websocket.OnlineStatusWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {
    private final OnlineStatusWebSocketHandler onlineStatusWebSocketHandler;
    private final ChatWebSocketHandler chatWebSocketHandler;
    private final WebSocketHandshakeInterceptor webSocketHandshakeInterceptor;

    @Value("${spring.host.front}")
    private String frontURL;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(onlineStatusWebSocketHandler, "/ws/online-status")
                .setAllowedOriginPatterns(frontURL);

        registry.addHandler(chatWebSocketHandler, "/ws/chat")
                .setAllowedOriginPatterns(frontURL)
                .addInterceptors(webSocketHandshakeInterceptor);
    }
}
