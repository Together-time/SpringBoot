package com.tt.Together_time.service;

import com.tt.Together_time.domain.dto.MemberDto;
import com.tt.Together_time.domain.dto.ProjectDto;
import com.tt.Together_time.domain.mongodb.ProjectDocument;
import com.tt.Together_time.domain.rdb.Member;
import com.tt.Together_time.domain.rdb.Project;
import com.tt.Together_time.domain.rdb.Team;
import com.tt.Together_time.repository.MemberRepository;
import com.tt.Together_time.repository.ProjectMongoRepository;
import com.tt.Together_time.repository.ProjectRepository;
import com.tt.Together_time.repository.TeamRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeamService {
    private final TeamRepository teamRepository;
    private final MemberRepository memberRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMongoRepository projectMongoRepository;
    private final ProjectDtoService projectDtoService;
    private final OnlineStatusService onlineStatusService;

    public List<ProjectDto> getProjects(String loggedEmail) {
        List<Project> projectList = teamRepository.findProjectsByMemberEmail(loggedEmail);

        return projectList.stream().map(project -> {
            ProjectDocument projectDocument = projectMongoRepository.findByProjectId(project.getId())
                            .orElseThrow(()->new EntityNotFoundException("해당 Project Document는 존재하지 않습니다."));
            return projectDtoService.convertToDto(project, projectDocument);
        }).collect(Collectors.toList());
    }

    @CacheEvict(value = "teamMembers", key = "#projectId")
    @CachePut(value = "teamMembers", key = "#projectId")
    public void addTeam(String logged, Member member, Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(()->new EntityNotFoundException());

        boolean isExistingMember = existsByProjectIdAndMemberEmail(projectId, logged);
        if(isExistingMember){
            Member newMember = memberRepository.findById(member.getId())
                    .orElseThrow(()->new EntityNotFoundException());

            teamRepository.save(
                    Team.builder()
                            .member(newMember)
                            .project(project)
                            .build()
            );
        }else
            throw new AccessDeniedException("초대 권한이 없습니다.");
    }

    //로그인한 사용자가 프로젝트에 속한 멤버인지
    public boolean existsByProjectIdAndMemberEmail(Long projectId, String email) {
        return teamRepository.existsByProjectIdAndMemberEmail(projectId, email);
    }

    public void addTeamByCreateProject(Member member, Project project) {
        //초대하려는 멤버가 유효한 멤버인지 검증
        Member newMember = memberRepository.findById(member.getId())
                .orElseThrow(()->new EntityNotFoundException());

        teamRepository.save(
                Team.builder()
                    .member(newMember)
                    .project(project)
                    .build()
        );
    }

    @CacheEvict(value = "teamMembers", key = "#projectId")
    @CachePut(value = "teamMembers", key = "#projectId")
    @Transactional
    public void leaveTeam(String logged, Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(()->new EntityNotFoundException());
        //권한 확인
        boolean isExistingMember = existsByProjectIdAndMemberEmail(project.getId(), logged);

        if(isExistingMember){
            teamRepository.deleteMemberByEmail(logged, project.getId());

            if(findByProjectId(projectId).size()==0)
                projectRepository.deleteById(project.getId());
        }
    }

    @Cacheable(value = "teamMembers", key = "#projectId", unless = "#result == null or #result.isEmpty()")
    public List<MemberDto> findByProjectId(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(()->new EntityNotFoundException("존재하지 않는 프로젝트입니다."));

        List<Team> teamList = teamRepository.findByProjectId(project.getId());

        List<Member> members = teamList.stream().map(Team::getMember).collect(Collectors.toList());

        List<MemberDto> memberDtos = members.stream().map(member -> {
            boolean isOnline = onlineStatusService.isOnline(member.getEmail());

            return new MemberDto(
                    member.getNickname(),
                    member.getEmail(),
                    "",
                    isOnline);
        }).collect(Collectors.toList());

        return memberDtos;
    }

    public void updateTeam(Project project, List<Member> members) {
        List<MemberDto> originMembers = findByProjectId(project.getId());
        //팀원 내보내기 기능이 없으므로 추가만
        for(Member member : members){
            if(originMembers.equals(member.getEmail()))
                continue;
            addTeamByCreateProject(member, project);
        }
    }
}
