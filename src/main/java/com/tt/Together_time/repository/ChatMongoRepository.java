package com.tt.Together_time.repository;

import com.tt.Together_time.domain.mongodb.ChatDocument;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface ChatMongoRepository extends MongoRepository<ChatDocument, String> {

    List<ChatDocument> findByProjectIdOrderByCreatedAtDesc(String projectId, Pageable pageable);

    long countByProjectIdAndUnreadByEmail(Long projectId, String email);

    List<ChatDocument> findByProjectIdAndUnreadByEmail(Long projectId, String email);

    void deleteByProjectId(Long projectId);
}
