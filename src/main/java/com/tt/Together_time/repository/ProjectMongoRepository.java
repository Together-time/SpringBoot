package com.tt.Together_time.repository;

import com.tt.Together_time.domain.mongodb.ProjectDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectMongoRepository extends MongoRepository<ProjectDocument, String> {
    List<ProjectDocument> findByTagsContaining(String tag);

    Optional<ProjectDocument> findByProjectId(Long projectId);
}
