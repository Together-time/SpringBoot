package com.tt.Together_time.service;

import com.tt.Together_time.domain.dto.ProjectDto;
import com.tt.Together_time.domain.mongodb.ProjectDocument;
import com.tt.Together_time.domain.rdb.Project;
import com.tt.Together_time.repository.ProjectMongoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProjectDtoService {
    private final ProjectMongoRepository projectMongoRepository;

    public ProjectDto convertToDto(Project project) {
        Optional<ProjectDocument> projectDocumentOptional = projectMongoRepository.findByProjectId(project.getId());
        List<String> tags = projectDocumentOptional.map(ProjectDocument::getTags).orElseGet(ArrayList::new);

        return new ProjectDto(
                project.getId(),
                project.getTitle(),
                project.getStatus(),
                project.getViews(),
                tags
        );
    }
}