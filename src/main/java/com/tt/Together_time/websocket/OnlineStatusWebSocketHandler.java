package com.tt.Together_time.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tt.Together_time.service.OnlineStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class OnlineStatusWebSocketHandler extends TextWebSocketHandler {
    private final OnlineStatusService onlineStatusService;
    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();

    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Authentication authentication = (Authentication) session.getPrincipal();
        if (authentication == null) {
            log.error("WebSocket 연결 실패: 인증되지 않은 사용자");
            session.close(CloseStatus.POLICY_VIOLATION);
            return;
        }

        String email = authentication.getPrincipal().toString();

        onlineStatusService.setOnline(email);
        sessions.add(session);

        sendCurrentOnlineUsers(session);
        broadcastOnlineStatus(email, true);
    }
    //연결 종료
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Authentication authentication = (Authentication) session.getPrincipal();
        if (authentication == null) {
            log.error("WebSocket 연결 실패: 인증되지 않은 사용자");
            session.close(CloseStatus.POLICY_VIOLATION);
            return;
        }

        String email = authentication.getPrincipal().toString();
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

    private void sendCurrentOnlineUsers(WebSocketSession session) throws Exception {
        Set<String> onlineUsers = onlineStatusService.getOnlineUsers();
        String message = String.format("{\"onlineUsers\":%s}", new ObjectMapper().writeValueAsString(onlineUsers));
        session.sendMessage(new TextMessage(message));
    }
}
