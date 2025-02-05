package com.tt.Together_time.service;

import com.tt.Together_time.domain.dto.ChatDto;
import com.tt.Together_time.domain.dto.MemberDto;
import com.tt.Together_time.domain.mongodb.ChatDocument;
import com.tt.Together_time.domain.rdb.Project;
import com.tt.Together_time.repository.ChatMongoRepository;
import com.tt.Together_time.repository.RedisDao;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ProjectService projectService;
    private final RedisDao redisDao;
    private final ChatMongoRepository chatMongoRepository;
    private final TeamService teamService;

    public void publishMessage(String projectId, String message) {
        Project project = projectService.findById(Long.valueOf(projectId));

        redisDao.publishMessage("chat:project:"+projectId, message);
    }

    public List<ChatDocument> getUnreadMessages(String projectId, String logged){
        return chatMongoRepository.findByProjectIdAndUnreadByContains(projectId, logged);
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
        List<MemberDto> teamMemberDtos = teamService.findByProjectId(Long.valueOf(chatDocument.getProjectId()));
        List<String> teamMembers = teamMemberDtos.stream().map(MemberDto::getEmail).collect(Collectors.toList());

        List<String> unreadBy = new ArrayList<>(teamMembers);
        unreadBy.remove(chatDocument.getSender().getEmail());

        chatDocument.setCreatedAt(LocalDateTime.now());
        chatDocument.setUnreadBy(new ArrayList<>(teamMembers));
        chatMongoRepository.save(chatDocument);
    }

    // 안 읽은 메시지 개수
    public long getUnreadMessageCount(String projectId, String logged) {
        return chatMongoRepository.countByProjectIdAndUnreadByContains(projectId, logged);
    }

    // 메시지 읽음 처리
    public void markMessagesAsRead(String projectId, String logged) {
        List<ChatDocument> unreadMessages = chatMongoRepository.findByProjectIdAndUnreadByContains(projectId, logged);

        for (ChatDocument message : unreadMessages) {
            message.getUnreadBy().remove(logged);
        }

        chatMongoRepository.saveAll(unreadMessages);
    }

    public void deleteByProjectId(Long projectId) {
        chatMongoRepository.deleteByProjectId(projectId);
    }
}
