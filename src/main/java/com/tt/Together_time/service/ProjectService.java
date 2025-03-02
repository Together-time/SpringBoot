package com.tt.Together_time.service;

import com.tt.Together_time.domain.dto.ProjectCommand;
import com.tt.Together_time.domain.dto.ProjectDto;
import com.tt.Together_time.domain.enums.ProjectSortType;
import com.tt.Together_time.domain.enums.ProjectVisibility;
import com.tt.Together_time.domain.mongodb.ProjectDocument;
import com.tt.Together_time.domain.rdb.Member;
import com.tt.Together_time.domain.rdb.Project;
import com.tt.Together_time.repository.ChatMongoRepository;
import com.tt.Together_time.repository.ProjectMongoRepository;
import com.tt.Together_time.repository.ProjectRepository;
import com.tt.Together_time.repository.RedisDao;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final ProjectMongoRepository projectMongoRepository;
    private final ChatMongoRepository chatMongoRepository;
    private final ProjectDtoService projectDtoService;
    private final TeamService teamService;
    private final MemberService memberService;
    private final RedisDao redisDao;

    @Transactional
    public void updateProjectTags(String logged, Long projectId, List<String> tags) {
        Project project = findById(projectId);
        boolean isExistingMember = teamService.existsByProjectIdAndMemberEmail(project.getId(), logged);

        if(isExistingMember)
            projectMongoRepository.replaceTags(project.getId(), tags);
        else
            throw new AccessDeniedException("권한이 없습니다.");
    }
    
    public ProjectDto getProject(Long projectId, String logged){
        Project project = findById(projectId);
        ProjectDocument projectDocument = projectMongoRepository.findByProjectId(project.getId())
                .orElseThrow(()->new EntityNotFoundException("해당 Project Document는 존재하지 않습니다."));
        ProjectDto projectDto = projectDtoService.convertToDto(project, projectDocument);

        if(!teamService.existsByProjectIdAndMemberEmail(projectId, logged)){
            Long views = viewProject(projectDocument, logged);
            projectDto.setViews(views);
        }

        return projectDto;
    }

    @Transactional
    public void addProject(ProjectCommand projectCommand, String logged) {
        Project project = projectRepository.save(
                Project.builder()
                        .title(projectCommand.getTitle())
                        .build()
        );

        Member generatedMember = memberService.findByEmail(logged);

        teamService.addTeamByCreateProject(generatedMember, project);

        for(Member member : projectCommand.getMembers()) {
            teamService.addTeamByCreateProject(member, project);
        }

        ProjectDocument projectDocument = new ProjectDocument(
                null,
                project.getId(),
                project.getTitle(),
                ProjectVisibility.PUBLIC,
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

        Project project = findById(projectId);
        ProjectDocument projectDocument = projectMongoRepository.findByProjectId(project.getId())
                .orElseThrow(()->new EntityNotFoundException("해당 Project Document는 존재하지 않습니다."));

        if (!project.getTitle().equals(title)) {
            projectRepository.updateProject(project.getId(), title);
            projectDocument.setTitle(title);
        }
        teamService.updateTeam(project, members);

        projectDocument.setTags(tags);
        projectMongoRepository.save(projectDocument);
    }

    public void updateProjectStatus(String logged, Long projectId) {
        ProjectDocument projectDocument = projectMongoRepository.findByProjectId(projectId).orElseThrow(()->new EntityNotFoundException("해당 Document는 존재하지 않습니다."));

        boolean isExistingMember = teamService.existsByProjectIdAndMemberEmail(projectId, logged);

        if(isExistingMember){
            ProjectVisibility newVisibility = (projectDocument.getStatus() == ProjectVisibility.PUBLIC)
                    ? ProjectVisibility.PRIVATE
                    : ProjectVisibility.PUBLIC;
            projectDocument.setStatus(newVisibility);
            projectMongoRepository.save(projectDocument);
        }else
            throw new AccessDeniedException("권한이 없습니다.");
    }

    public void deleteById(String logged, Long projectId) {
        Project project = findById(projectId);

        boolean isExistingMember = teamService.existsByProjectIdAndMemberEmail(project.getId(), logged);

        if(isExistingMember){
            projectRepository.deleteById(projectId);
            projectMongoRepository.deleteByProjectId(projectId);
            chatMongoRepository.deleteByProjectId(projectId);
        }else
            throw new AccessDeniedException("권한이 없습니다.");
    }

    public List<ProjectDocument> findProjectsByKeyword(String keyword, ProjectSortType projectSortType) {

        log.info("here0");
        List<ProjectDocument> projectsBytags = projectMongoRepository.searchByTitleOrTags(keyword, projectSortType.getSort());
        log.info("{}", projectsBytags.size());
        return projectsBytags;
    }

    @Transactional
    public Long viewProject(ProjectDocument projectDocument, String logged){
        String redisKey = "views:"+String.valueOf(projectDocument.getProjectId());
        String userViewKey = "user_views:" + logged;

        String values = redisDao.getValues(redisKey);
        Long views = (values != null) ? Long.parseLong(values) : projectDocument.getViews();

        if(!redisDao.isMember(userViewKey, redisKey)){
            redisDao.addToSet(userViewKey, redisKey);
            views = redisDao.increment(redisKey);
        }

        redisDao.setValuesWithTTL(redisKey, String.valueOf(views), 3600);

        return views;
    }

    public Project findById(Long projectId){
        return projectRepository.findById(projectId).orElseThrow(()->new EntityNotFoundException("해당 프로젝트는 존재하지 않습니다."));
    }
}
