package com.tt.Together_time.config;

import com.tt.Together_time.websocket.ChatWebSocketHandler;
import com.tt.Together_time.websocket.OnlineStatusWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
    private final WebSocketHandshakeInterceptor webSocketHandshakeInterceptor;

    @Value("${spring.host.front}")
    private String frontURL;

    /*@PostConstruct
    public void check() {
        log.info("✅ WebSocketConfig가 정상적으로 로드되었습니다!");
    }
*/
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(webSocketHandler, "/ws/online-status")
                .setAllowedOriginPatterns(frontURL)
                .withSockJS();

        registry.addHandler(chatWebSocketHandler, "/ws/chat")
                .setAllowedOriginPatterns(frontURL)
                .addInterceptors(webSocketHandshakeInterceptor)
                .withSockJS();
    }
}
