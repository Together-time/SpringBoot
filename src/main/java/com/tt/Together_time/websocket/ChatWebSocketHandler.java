package com.tt.Together_time.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tt.Together_time.domain.dto.MemberDto;
import com.tt.Together_time.domain.dto.Sender;
import com.tt.Together_time.domain.mongodb.ChatDocument;
import com.tt.Together_time.domain.dto.ChatDto;
import com.tt.Together_time.service.ChatService;
import com.tt.Together_time.service.TeamService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final ChatService chatService;
    private final ObjectMapper objectMapper;
    private final TeamService teamService;

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final Map<Long, Set<String>> connectedUsers = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws IOException {
        //sessions.put(session.getId(), session);

        Map<String, Object> attributes = session.getAttributes();
        String email = (String) attributes.get("email");
        Long projectId = (Long) attributes.get("projectId");

        if (email != null && projectId != null) {
            connectedUsers.putIfAbsent(projectId, new HashSet<>());
            connectedUsers.get(projectId).add(email);

            List<ChatDto> latestMessages = chatService.getLatestMessages(projectId)
                    .stream()
                    .map(ChatDto::new)
                    .collect(Collectors.toList());
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(latestMessages)));

            chatService.markMessagesAsRead(projectId, email);

            List<ChatDto> updatedMessages = chatService.getUnreadMessages(projectId, email)
                    .stream()
                    .map(ChatDto::new)
                    .collect(Collectors.toList());

            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(updatedMessages)));

            String readNotification = String.format("{\"type\": \"read\", \"projectId\": %d, \"email\": \"%s\"}", projectId, email);
            chatService.publishMessage(String.valueOf(projectId), readNotification, "read");
        }
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        //type : send/read
        String payload = message.getPayload().toString();
        JsonNode jsonNode = objectMapper.readTree(payload);

        if (jsonNode == null) {
            System.out.println("Invalid message format: " + payload);
            return;
        }

        Map<String, Object> attributes = session.getAttributes();
        Long projectId = (Long) attributes.get("projectId");

        ChatDocument chatDocument = objectMapper.readValue(payload, ChatDocument.class);
        List<MemberDto> teamMembers = teamService.findByProjectId(projectId);

        List<Sender> unread = teamMembers.stream()
                .map(member->new Sender(member.getNickname(), member.getEmail()))
                .filter(member -> !connectedUsers.getOrDefault(projectId, new HashSet<>())
                        .contains(member.getEmail()))
                .collect(Collectors.toList());
        chatDocument.setUnreadBy(unread);
        chatDocument.setProjectId(projectId);
        chatService.publishMessage(String.valueOf(projectId), payload, "send");
        chatService.saveMessageToMongoDB(chatDocument);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        System.out.println("에러 발생: " + exception.getMessage());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        //sessions.remove(session.getId());
        //System.out.println("연결 종료: " + session.getId());

        Map<String, Object> attributes = session.getAttributes();
        String email = (String) attributes.get("email");
        Long projectId = (Long) attributes.get("projectId");
        if (email != null && projectId != null) {
            Set<String> users = connectedUsers.get(projectId);
            if (users != null) {
                users.remove(email);
                if (users.isEmpty()) {
                    connectedUsers.remove(projectId);
                }
            }
        }
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
