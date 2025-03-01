package com.tt.Together_time.websocket;

import com.tt.Together_time.service.OnlineStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class OnlineStatusWebSocketHandler extends TextWebSocketHandler {
    private final OnlineStatusService onlineStatusService;
    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();

    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        OAuth2AuthenticationToken authenticationToken = (OAuth2AuthenticationToken) session.getPrincipal();
        OAuth2User oAuth2User = authenticationToken.getPrincipal();
        Map<String, Object> kakaoAccount = oAuth2User.getAttribute("kakao_account");
        String email = (String) kakaoAccount.get("email");
        onlineStatusService.setOnline(email);
        sessions.add(session);
        broadcastOnlineStatus(email, true);
    }
    //연결 종료
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String email = session.getPrincipal().getName();
        onlineStatusService.setOffline(email);
        sessions.remove(session);
        broadcastOnlineStatus(email, false);
    }
    // 상태 변경 알림 전송
    private void broadcastOnlineStatus(String email, boolean isOnline) throws Exception {
        String message = String.format("{\"email\":\"%s\", \"isOnline\":%b}", email, isOnline);
        for (WebSocketSession session : sessions) {
            session.sendMessage(new TextMessage(message));
        }
    }
}
