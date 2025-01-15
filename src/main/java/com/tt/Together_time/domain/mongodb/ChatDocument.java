package com.tt.Together_time.domain.mongodb;

import com.tt.Together_time.domain.rdb.Member;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "messages")
@Getter
@Setter
@AllArgsConstructor
public class ChatDocument {
    @Id
    private String id;

    private String content;
    private LocalDateTime createdAt;
    private Member sender;
    private String projectId;
}
