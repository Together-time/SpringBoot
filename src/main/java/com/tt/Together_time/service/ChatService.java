package com.tt.Together_time.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.tt.Together_time.domain.mongodb.ChatDocument;
import com.tt.Together_time.repository.ChatMongoRepository;
import com.tt.Together_time.repository.RedisDao;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final RedisDao redisDao;
    private final ChatMongoRepository chatMongoRepository;
    private final MongoTemplate mongoTemplate;

    public void publishMessage(String projectId, String message, String type) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> messageMap = new HashMap<>();
            messageMap.put("type", type);
            messageMap.put("projectId", projectId);
            messageMap.put("chat", message);

            String jsonMessage = objectMapper.writeValueAsString(messageMap);
            String channel = "chat:project:" + projectId;
            redisDao.publishMessage(channel, jsonMessage);

        } catch (JsonProcessingException e) {
            System.err.println("메시지 변환 오류: " + e.getMessage());
        }
    }

    public List<ChatDocument> getLatestMessages(Long projectId) {
        Query query = new Query(Criteria.where("projectId").is(projectId))
                .with(Sort.by(Sort.Direction.DESC, "createdAt")) // 최신순 정렬
                .limit(30);

        return mongoTemplate.find(query, ChatDocument.class);
    }

    public List<ChatDocument> getMessagesBefore(Long projectId, LocalDateTime before) {
        Query query = new Query(Criteria.where("projectId").is(projectId)
                .and("createdAt").lt(before))
                .with(Sort.by(Sort.Direction.DESC, "createdAt"))
                .limit(30);

        return mongoTemplate.find(query, ChatDocument.class);
    }

    public void saveMessageToMongoDB(ChatDocument chatDocument) {
        chatMongoRepository.save(chatDocument);
    }

    // 메시지 읽음 처리
    public void markMessagesAsRead(Long projectId, String logged) {
        Query query = new Query(Criteria.where("projectId").is(projectId)
                .and("unreadBy.email").is(logged));
        Update update = new Update().pull("unreadBy", new BasicDBObject("email", logged));
        mongoTemplate.updateMulti(query, update, ChatDocument.class);
    }

    public List<ChatDocument> getUnreadMessages(Long projectId, String logged){
        Query query = new Query(Criteria.where("projectId").is(projectId)
                .and("unreadBy.email").is(logged));
        return mongoTemplate.find(query, ChatDocument.class);
    }

    // 안 읽은 메시지 개수
    public long getUnreadMessageCount(Long projectId, String logged) {
        return chatMongoRepository.countByProjectIdAndUnreadByEmail(projectId, logged);
    }
}
