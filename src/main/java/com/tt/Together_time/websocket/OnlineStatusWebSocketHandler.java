package com.tt.Together_time.websocket;

import com.tt.Together_time.service.OnlineStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class OnlineStatusWebSocketHandler extends TextWebSocketHandler {
    private final OnlineStatusService onlineStatusService;
    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();

    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String email = session.getPrincipal().getName();
        onlineStatusService.setOnline(email);  // 온라인 상태 등록
        sessions.add(session);
        //session.sendMessage(new TextMessage("접속한 사용자 : "+email));
        broadcastOnlineStatus(email, true);
    }
    //연결 종료
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String email = session.getPrincipal().getName();
        onlineStatusService.setOffline(email);  // 온라인 상태 제거
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
