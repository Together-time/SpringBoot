package com.tt.Together_time.service;

import com.tt.Together_time.domain.dto.ChatDto;
import com.tt.Together_time.domain.mongodb.ChatDocument;
import com.tt.Together_time.domain.rdb.Project;
import com.tt.Together_time.repository.ChatMongoRepository;
import com.tt.Together_time.repository.RedisDao;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ProjectService projectService;
    private final RedisDao redisDao;
    private final ChatMongoRepository chatMongoRepository;

    public void publishMessage(String projectId, String message) {
        Project project = projectService.findById(Long.valueOf(projectId));

        redisDao.publishMessage("chat:project:"+projectId, message);
    }

    public List<ChatDocument> getUnreadMessages(Long projectId, String logged){
        return chatMongoRepository.findByProjectIdAndUnreadByEmail(projectId, logged);
    }

    public List<ChatDto> getChatMessages(Long projectId, long start, long end) {
        int limit = (int) (end - start + 1); // 가져올 메시지 개수
        int page = (int) (start / limit);   // 현재 페이지 계산

        Pageable pageable = PageRequest.of(page, limit, Sort.by(Sort.Direction.DESC, "createdAt"));

        List<ChatDocument> chatDocuments = chatMongoRepository.findByProjectIdOrderByCreatedAtDesc(String.valueOf(projectId), pageable);

        return chatDocuments.stream()
                .map(ChatDto::new)
                .collect(Collectors.toList());
    }
    public void saveMessageToMongoDB(ChatDocument chatDocument) {
        chatMongoRepository.save(chatDocument);
    }

    // 안 읽은 메시지 개수
    public long getUnreadMessageCount(Long projectId, String logged) {
        return chatMongoRepository.countByProjectIdAndUnreadByEmail(projectId, logged);
    }

    // 메시지 읽음 처리
    public void markMessagesAsRead(Long projectId, String logged) {
        List<ChatDocument> unreadMessages = chatMongoRepository.findByProjectIdAndUnreadByEmail(projectId, logged);

        unreadMessages.forEach(chat -> {
            chat.getUnreadBy().removeIf(sender -> sender.getEmail().equals(logged));
        });


        chatMongoRepository.saveAll(unreadMessages);
    }
}
