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
import com.tt.Together_time.repository.RedisDao;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final ProjectMongoRepository projectMongoRepository;
    private final TeamService teamService;
    private final ProjectDtoService projectDtoService;
    private final RedisDao redisDao;


    public Optional<ProjectDocument> findTagsByProjectId(Long projectId){
        return projectMongoRepository.findByProjectId(projectId);
    }

    public void updateProjectTags(String logged, Long projectId, List<String> tags) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(()-> new EntityNotFoundException());
        boolean isExistingMember = teamService.existsByProjectIdAndMemberEmail(project.getId(), logged);

        if(isExistingMember)
            projectMongoRepository.replaceTags(projectId, project.getTitle(), tags);
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
                        .views(0L)
                        .build()
        );

        for(Member member : projectCommand.getMembers()) {
            teamService.addTeamByCreateProject(member, project);
        }

        ProjectDocument projectDocument = new ProjectDocument(
                null,
                project.getId(),
                project.getTitle(),
                projectCommand.getTags(),
                0L,
                LocalDateTime.now()
                );
        projectMongoRepository.save(projectDocument);
    }

    @Transactional
    public void updateProject(Long projectId, ProjectCommand projectCommand) {
        String title = projectCommand.getTitle();
        List<Member> members = projectCommand.getMembers();
        List<String> tags = projectCommand.getTags();

        Project project = projectRepository.findById(projectId)
                .orElseThrow(()-> new EntityNotFoundException());
        if(project != null) {
            if (!project.getTitle().equals(title))
                projectRepository.updateProject(project.getId(), title);
            teamService.updateTeam(project, members);
            projectMongoRepository.replaceTags(project.getId(), title, tags);
        }
    }

    public void updateProjectStatus(String logged, Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(()-> new EntityNotFoundException());

        boolean isExistingMember = teamService.existsByProjectIdAndMemberEmail(projectId, logged);

        if(isExistingMember){
            ProjectVisibility newVisibility = (project.getStatus() == ProjectVisibility.PUBLIC)
                    ? ProjectVisibility.PRIVATE
                    : ProjectVisibility.PUBLIC;
            projectRepository.updateProjectStatus(projectId, newVisibility);
        }else
            throw new AccessDeniedException("권한이 없습니다.");
    }

    public void deleteById(String logged, Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(()-> new EntityNotFoundException());

        boolean isExistingMember = teamService.existsByProjectIdAndMemberEmail(projectId, logged);

        if(isExistingMember){
            projectRepository.deleteById(projectId);
        }else
            throw new AccessDeniedException("권한이 없습니다.");
    }

    public List<ProjectDocument> findProjectsByKeyword(String keyword) {
        Sort sortByCreatedAt = Sort.by(Sort.Direction.DESC, "createdAt");
        List<ProjectDocument> projectsBytags = projectMongoRepository.searchByTitleOrTags(keyword, sortByCreatedAt);

        return projectsBytags;
    }

    //조회수
    @Transactional
    public Long viewProject(String logged, Long projectId){
        //같은 사용자에 대해 조회수가 1회만 증가하도록 함
        Project project = projectRepository.findById(projectId)
                .orElseThrow(()-> new EntityNotFoundException());

        String redisKey = "views:"+String.valueOf(projectId);
        String values = redisDao.getValues(redisKey);
        if(values==null || values.equals("0")){
            values = project.getViews()>0? String.valueOf(project.getViews()):String.valueOf(0);
        }

        Long views = Long.valueOf(values);

        if(!redisDao.getValuesList(logged).contains(redisKey)){
            redisDao.setValuesList(logged, redisKey);
            views++;
            redisDao.setValues(redisKey, String.valueOf(views));

        }
        return views;
    }
}
