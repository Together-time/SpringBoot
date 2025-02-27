package com.tt.Together_time.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tt.Together_time.domain.dto.ChatDto;
import com.tt.Together_time.websocket.ChatWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class RedisMessageSubscriber implements MessageListener {
    private final ObjectMapper objectMapper;
    private final ChatWebSocketHandler chatWebSocketHandler;
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String payload = new String(message.getBody());
            JsonNode jsonNode = objectMapper.readTree(payload);
            String type = jsonNode.get("type").asText();

            String chatString = jsonNode.get("chat").asText();
            JsonNode chatJsonNode = objectMapper.readTree(chatString);

            if(type.equals("send")){
                ChatDto chatDto = objectMapper.treeToValue(chatJsonNode, ChatDto.class);
                chatWebSocketHandler.broadcastMessage(chatDto);
            }else if(type.equals("read")){
                Long projectId = jsonNode.get("projectId").asLong();
                String email = chatJsonNode.get("email").asText();

                // 프로젝트에 연결된 세션 중 현재 접속한 사용자에게만 읽음 처리 보냄
                chatWebSocketHandler.notifyReadStatus(projectId, email);
            }
        } catch (Exception e) {
            System.err.println("RedisMessageSubscriber error: " + e.getMessage());
        }
    }
}