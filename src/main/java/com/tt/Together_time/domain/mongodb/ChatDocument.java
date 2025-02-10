package com.tt.Together_time.domain.mongodb;

import com.tt.Together_time.domain.dto.Sender;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "messages")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChatDocument {
    @Id
    private String id;

    private String content;

    @CreatedDate
    private LocalDateTime createdAt;

    private Sender sender;
    private Long projectId;
    private List<Sender> unreadBy = new ArrayList<>();
}
