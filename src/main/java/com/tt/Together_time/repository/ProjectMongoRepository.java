package com.tt.Together_time.repository;

import com.tt.Together_time.domain.mongodb.ProjectDocument;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;

import java.util.List;
import java.util.Optional;

public interface ProjectMongoRepository extends MongoRepository<ProjectDocument, String> {
    List<ProjectDocument> findByTagsContaining(String tag);

    Optional<ProjectDocument> findByProjectId(Long projectId);

    @Modifying
    @Query("{ 'projectId': ?0 }")
    @Update("{ '$set': { 'tags': ?1 } }")
    void replaceTags(Long projectId, List<String> tags);
}
