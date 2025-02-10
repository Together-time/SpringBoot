package com.tt.Together_time.repository;

import com.tt.Together_time.domain.mongodb.ChatDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ChatMongoRepository extends MongoRepository<ChatDocument, String> {
    long countByProjectIdAndUnreadByEmail(Long projectId, String email);

    void deleteByProjectId(Long projectId);
}
