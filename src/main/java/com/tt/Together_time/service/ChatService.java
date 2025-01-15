package com.tt.Together_time.service;

import com.tt.Together_time.domain.mongodb.ChatDocument;
import com.tt.Together_time.repository.ChatMongoRepository;
import com.tt.Together_time.repository.RedisDao;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final RedisDao redisDao;
    private final ChatMongoRepository chatMongoRepository;

    private static final int MAX_REDIS_MESSAGES = 100;

    public void publishMessage(String projectId, String message) {
        redisDao.publishMessage("chat:project:"+projectId, message);
    }

    public List<ChatDocument> getChatMessages(Long projectId, long start, long end) {
        int limit = (int) (end - start + 1); // 가져올 메시지 개수
        int page = (int) (start / limit);   // 현재 페이지 계산

        Pageable pageable = PageRequest.of(page, limit, Sort.by(Sort.Direction.DESC, "createdAt"));

        return chatMongoRepository.findByProjectIdOrderByCreatedAtDesc(String.valueOf(projectId), pageable);
    }
    public void saveMessageToMongoDB(ChatDocument chatDocument) {
        chatDocument.setCreatedAt(LocalDateTime.now()); // 생성 시간 설정
        chatMongoRepository.save(chatDocument);
    }
}
