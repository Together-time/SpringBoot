package com.tt.Together_time.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tt.Together_time.domain.mongodb.ChatDocument;
import com.tt.Together_time.domain.dto.ChatDto;
import com.tt.Together_time.service.ChatService;
import com.tt.Together_time.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final ChatService chatService;
    private final ObjectMapper objectMapper;
    private final MemberService memberService;

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.put(session.getId(), session);
        System.out.println("새 연결: " + session.getId());
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        //type : send/read
        String payload = message.getPayload().toString();
        JsonNode jsonNode = objectMapper.readTree(payload);
        String type = jsonNode.get("type").asText();

        if (jsonNode == null || !jsonNode.has("type")) {
            System.out.println("Invalid message format: " + payload);
            return;
        }

        if(type.equals("send")){
            ChatDocument chatDocument = objectMapper.readValue(payload, ChatDocument.class);
            if (chatDocument.getCreatedAt() == null) {
                chatDocument.setCreatedAt(LocalDateTime.now());
            }
            chatService.publishMessage(chatDocument.getProjectId(), payload);
            chatService.saveMessageToMongoDB(chatDocument);
        }else if(type.equals("read")){
            String projectId = jsonNode.get("projectId").asText();
            String loggedInMember = memberService.getUserEmail();

            List<ChatDocument> unreadMessages = chatService.getUnreadMessages(projectId, loggedInMember);
            chatService.markMessagesAsRead(projectId, loggedInMember);

            for (ChatDocument chat : unreadMessages) {
                broadcastMessage(chat);
            }
        }
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

    private void broadcastMessage(ChatDocument chatDocument) throws Exception {
        String messageJson = objectMapper.writeValueAsString(new ChatDto(chatDocument));

        for (WebSocketSession session : sessions.values()) {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(messageJson));
            }
        }
    }
}
