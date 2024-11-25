package com.tt.Together_time.service;

import com.tt.Together_time.domain.dto.MemberDto;
import com.tt.Together_time.domain.dto.ProjectCommand;
import com.tt.Together_time.domain.dto.ProjectDto;
import com.tt.Together_time.domain.enums.ProjectVisibility;
import com.tt.Together_time.domain.mongodb.ProjectDocument;
import com.tt.Together_time.domain.rdb.Member;
import com.tt.Together_time.domain.rdb.Project;
import com.tt.Together_time.repository.ProjectMongoRepository;
import com.tt.Together_time.repository.ProjectRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final ProjectMongoRepository projectMongoRepository;
    private final TeamService teamService;
    private final ProjectDtoService projectDtoService;


    public Optional<ProjectDocument> findTagsByProjectId(Long projectId){
        return projectMongoRepository.findByProjectId(projectId);
    }

    public List<ProjectDocument> findProjectsByTag(String tag){
        return projectMongoRepository.findByTagsContaining(tag);
    }

    public void updateProjectTags(MemberDto logged, Long projectId, List<String> tags) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(()-> new EntityNotFoundException());
        boolean isExistingMember = teamService.existsByProjectIdAndMemberEmail(project.getId(), logged.getEmail());

        if(isExistingMember)
            projectMongoRepository.replaceTags(projectId, tags);
        else
            throw new AccessDeniedException("권한이 없습니다.");
    }
    
    public ProjectDto getProject(Long projectId){
        Project project = projectRepository.findById(projectId)
                .orElseThrow(()-> new EntityNotFoundException());

        ProjectDto projectDto = projectDtoService.convertToDto(project);

        //조회수 증가

        ProjectDocument projectDocument = findTagsByProjectId(projectId).get();
        projectDto.setTags(projectDocument.getTags());

        return projectDto;
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
            System.out.println("member "+member.getNickname());
            teamService.addTeamByCreateProject(member, project);
        }

        ProjectDocument projectDocument = new ProjectDocument(null, project.getId(), projectCommand.getTags());
        projectMongoRepository.save(projectDocument);
    }

    @Transactional
    public void updateProject(Long projectId, String title, List<String> tags) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(()-> new EntityNotFoundException());
        if(project != null) {
            projectRepository.updateProject(projectId, title);
            projectMongoRepository.replaceTags(projectId, tags);
        }
    }

    public void updateProjectStatus(MemberDto logged, Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(()-> new EntityNotFoundException());

        boolean isExistingMember = teamService.existsByProjectIdAndMemberEmail(projectId, logged.getEmail());

        if(isExistingMember){
            ProjectVisibility newVisibility = (project.getStatus() == ProjectVisibility.PUBLIC)
                    ? ProjectVisibility.PRIVATE
                    : ProjectVisibility.PUBLIC;
            projectRepository.updateProjectStatus(projectId, newVisibility);
        }else
            throw new AccessDeniedException("권한이 없습니다.");
    }

    public void deleteById(MemberDto logged, Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(()-> new EntityNotFoundException());

        boolean isExistingMember = teamService.existsByProjectIdAndMemberEmail(projectId, logged.getEmail());

        if(isExistingMember){
            projectRepository.deleteById(projectId);
        }else
            throw new AccessDeniedException("권한이 없습니다.");
    }
}
