package com.tt.Together_time.service;

import com.tt.Together_time.domain.dto.ProjectDto;
import com.tt.Together_time.domain.mongodb.ProjectDocument;
import com.tt.Together_time.domain.rdb.Project;
import com.tt.Together_time.repository.ProjectMongoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProjectDtoService {
    private final ProjectMongoRepository projectMongoRepository;

    public ProjectDto convertToDto(Project project, ProjectDocument projectDocument) {
        return ProjectDto.builder()
                .id(project.getId())
                .title(project.getTitle())
                .status(projectDocument.getStatus())
                .views(projectDocument.getViews())
                .tags(projectDocument.getTags())
                .build();
    }
}