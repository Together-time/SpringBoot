package com.tt.Together_time.domain.dto;

import com.tt.Together_time.domain.mongodb.ChatDocument;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ChatDto {
    private String id;
    private String content;
    private LocalDateTime createdAt;
    private Sender sender;
    private Long projectId;
    private int unreadCount;

    public ChatDto(ChatDocument chatDocument) {
        this.id = chatDocument.getId();
        this.content = chatDocument.getContent();
        this.createdAt = chatDocument.getCreatedAt();
        this.sender = chatDocument.getSender();
        this.projectId = chatDocument.getProjectId();
        this.unreadCount = chatDocument.getUnreadBy().size();
    }
}
