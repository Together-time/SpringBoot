package com.tt.Together_time.service;

import com.fasterxml.jackson.databind.ObjectMapper;
       import com.tt.Together_time.domain.mongodb.ChatDocument;
import com.tt.Together_time.websocket.ChatWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class RedisMessageSubscriber implements MessageListener {
    private final ObjectMapper objectMapper;
    private final ChatWebSocketHandler chatWebSocketHandler;//
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String payload = new String(message.getBody());
            ChatDocument chatMessage = objectMapper.readValue(payload, ChatDocument.class);

            chatWebSocketHandler.broadcastMessage(chatMessage);
            /*for (WebSocketSession session : sessions.values()) {    //웹소켓 세션에 브로드캐스트
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(objectMapper.writeValueAsString(chatMessage)));
                }
            }*/
        } catch (Exception e) {
            System.err.println("RedisMessageSubscriber error: " + e.getMessage());
        }
    }
}