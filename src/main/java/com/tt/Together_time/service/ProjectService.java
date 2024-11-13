package com.tt.Together_time.service;

import com.tt.Together_time.domain.dto.ProjectCommand;
import com.tt.Together_time.domain.dto.ProjectDto;
import com.tt.Together_time.domain.enums.ProjectVisibility;
import com.tt.Together_time.domain.mongodb.ProjectDocument;
import com.tt.Together_time.domain.rdb.Member;
import com.tt.Together_time.domain.rdb.Project;
import com.tt.Together_time.repository.ProjectMongoRepository;
import com.tt.Together_time.repository.ProjectRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final ProjectMongoRepository projectMongoRepository;
    private final TeamService teamService;
    private final ProjectDtoService projectDtoService;

    public ProjectDto getProject(Long projectId){
        Project project = findById(projectId).get();

        return convertToDto(project);
    }

    public Optional<Project> findById(Long projectId) {
        return projectRepository.findById(projectId);
    }

    public Optional<ProjectDocument> findTagsByProjectId(Long projectId){
        return projectMongoRepository.findByProjectId(projectId);
    }

    public List<ProjectDocument> findProjectsByTag(String tag){
        return projectMongoRepository.findByTagsContaining(tag);
    }

    @Transactional
    public void addProject(ProjectCommand projectCommand) {
        Project project = projectRepository.save(
                Project.builder()
                        .title(projectCommand.getTitle())
                        .status(ProjectVisibility.PUBLIC)
                        .build()
        );

        for(Member member : projectCommand.getMembers()) {
            teamService.addTeam(member, project);
        }

        ProjectDocument projectDocument = new ProjectDocument(null, project.getId(), projectCommand.getTags());
        projectMongoRepository.save(projectDocument);
    }

    public ProjectDto convertToDto(Project project) {
        return projectDtoService.convertToDto(project);
    }
}
