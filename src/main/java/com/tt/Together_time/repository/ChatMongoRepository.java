package com.tt.Together_time.repository;

import com.tt.Together_time.domain.mongodb.ChatDocument;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ChatMongoRepository extends MongoRepository<ChatDocument, String> {

    List<ChatDocument> findByProjectIdOrderByCreatedAtDesc(String projectId, Pageable pageable);
}
