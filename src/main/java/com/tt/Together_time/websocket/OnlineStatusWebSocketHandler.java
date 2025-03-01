package com.tt.Together_time.websocket;

import com.tt.Together_time.service.MemberService;
import com.tt.Together_time.service.OnlineStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
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
    private final MemberService memberService;
    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();

    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String email = "";
        Authentication authentication = (Authentication) session.getPrincipal();
        if (authentication instanceof OAuth2AuthenticationToken) {
            OAuth2User oauth2User = (OAuth2User) ((OAuth2AuthenticationToken) authentication).getPrincipal();
            email = oauth2User.getAttribute("email");  // 카카오 로그인 시 이메일 가져오기
        }

        onlineStatusService.setOnline(email);
        sessions.add(session);
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
