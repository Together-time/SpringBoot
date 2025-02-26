package com.tt.Together_time.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tt.Together_time.domain.dto.ChatDto;
import com.tt.Together_time.domain.dto.MemberDto;
import com.tt.Together_time.domain.dto.Sender;
import com.tt.Together_time.domain.mongodb.ChatDocument;
import com.tt.Together_time.service.ChatService;
import com.tt.Together_time.service.TeamService;
import lombok.RequiredArgsConstructor;
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
        sessions.put(session.getId(), session);

        Map<String, Object> attributes = session.getAttributes();
        String email = (String) attributes.get("email");
        Long projectId = (Long) attributes.get("projectId");

        if (email != null && projectId != null) {
            connectedUsers.putIfAbsent(projectId, new HashSet<>());
            connectedUsers.get(projectId).add(email);

            String readNotification = String.format("{\"type\": \"read\", \"projectId\": %d, \"email\": \"%s\"}", projectId, email);
            chatService.publishMessage(String.valueOf(projectId), readNotification, "read");

            List<ChatDto> latestMessages = chatService.getLatestMessages(projectId).stream()
                    .map(ChatDto::new)
                    .collect(Collectors.toList());
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(latestMessages)));
        }
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
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

        Set<String> connectedEmails = connectedUsers.getOrDefault(projectId, new HashSet<>());

        List<Sender> unread = teamMembers.stream()
                .map(member->new Sender(member.getNickname(), member.getEmail()))
                .filter(member -> !connectedEmails.contains(member.getEmail()))
                .collect(Collectors.toList());
        chatDocument.setUnreadBy(unread);
        chatDocument.setProjectId(projectId);

        ChatDto chatDto = new ChatDto(chatDocument);

        chatService.publishMessage(String.valueOf(projectId), objectMapper.writeValueAsString(chatDto), "send");
        chatService.saveMessageToMongoDB(chatDocument);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        System.out.println("에러 발생: " + exception.getMessage());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session.getId());

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

    public void broadcastMessage(ChatDocument chatDocument) throws Exception {
        String messageJson = objectMapper.writeValueAsString(chatDocument);

        for (WebSocketSession session : sessions.values()) {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(messageJson));
            }
        }
    }
    // 읽음 처리를 프로젝트 내 다른 사용자들에게 브로드캐스트
    public void notifyReadStatus(Long projectId, String email) {
        List<ChatDocument> unreadMessages = chatService.getUnreadMessages(projectId, email);

        if(!unreadMessages.isEmpty())
            chatService.markMessagesAsRead(projectId, email);

        for (ChatDocument chatDocument : unreadMessages) {
            int unreadCount = chatDocument.getUnreadBy().size();

            String readJson = String.format(
                    "{\"type\":\"read\",\"projectId\":%d,\"messageId\":\"%s\",\"unreadCount\":%d}",
                    projectId, chatDocument.getId(), unreadCount
            );

            sessions.values().stream()
                    .filter(session -> {
                        String sessionEmail = (String) session.getAttributes().get("email");
                        return !email.equals(sessionEmail);
                    })
                    .forEach(session -> {
                        try {
                            session.sendMessage(new TextMessage(readJson));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
        }
    }
}
