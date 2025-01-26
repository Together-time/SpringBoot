package com.tt.Together_time.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tt.Together_time.domain.mongodb.ChatDocument;
import com.tt.Together_time.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler implements WebSocketHandler {

    private final ChatService chatService;
    private final ObjectMapper objectMapper;

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.put(session.getId(), session);
        System.out.println("새 연결: " + session.getId());
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        String payload = message.getPayload().toString();
        ChatDocument chatDocument = objectMapper.readValue(payload, ChatDocument.class);
        if (chatDocument.getCreatedAt() == null) {
            chatDocument.setCreatedAt(LocalDateTime.now()); // 현재 시간으로 설정
        }
        chatService.publishMessage(chatDocument.getProjectId(), payload);
        chatService.saveMessageToMongoDB(chatDocument);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        System.out.println("에러 발생: " + exception.getMessage());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session.getId());
        System.out.println("연결 종료: " + session.getId());
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
}
