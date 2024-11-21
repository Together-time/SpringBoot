package com.tt.Together_time.service;

import com.tt.Together_time.domain.dto.MemberDto;
import com.tt.Together_time.domain.dto.ProjectDto;
import com.tt.Together_time.domain.rdb.Member;
import com.tt.Together_time.domain.rdb.Project;
import com.tt.Together_time.domain.rdb.Team;
import com.tt.Together_time.repository.MemberRepository;
import com.tt.Together_time.repository.ProjectRepository;
import com.tt.Together_time.repository.TeamRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeamService {
    private final TeamRepository teamRepository;
    private final MemberRepository memberRepository;
    private final ProjectRepository projectRepository;
    private final ProjectDtoService projectDtoService;

    public List<ProjectDto> getProjects(String loggedEmail) {
        List<Project> projectList = teamRepository.findProjectsByMemberEmail(loggedEmail);
        return projectList.stream().map(projectDtoService::convertToDto).collect(Collectors.toList());
    }

    public void addTeam(MemberDto logged, String inviteMember, Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(()->new EntityNotFoundException());
        //초대 권한 확인
        boolean isExistingMember = existsByProjectIdAndMemberEmail(projectId, logged.getEmail());
        if(isExistingMember){
            Member newMember = memberRepository.findMember(inviteMember)
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

    public boolean existsByProjectIdAndMemberEmail(Long projectId, String email) {
        return teamRepository.existsByProjectIdAndMemberEmail(projectId, email);
    }

    public void addTeam(Member member, Project project) {
        //초대하려는 유효한 멤버인지 검증
        Member newMember = memberRepository.findById(member.getId())
                .orElseThrow(()->new EntityNotFoundException());

        teamRepository.save(
                Team.builder()
                    .member(newMember)
                    .project(project)
                    .build()
        );
    }

    @Transactional
    public void removeTeam(MemberDto memberDto, Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(()->new EntityNotFoundException());

        if(project != null){
            teamRepository.deleteMemberByEmail(memberDto.getEmail(), projectId);

            if(findByProjectId(projectId).size()==0)
                projectRepository.deleteById(projectId);
        }
    }

    public List<Member> findByProjectId(Long projectId) {
        List<Team> teamList = teamRepository.findByProjectId(projectId);

        List<Member> members = teamList.stream().map(Team::getMember).collect(Collectors.toList());

        return members;
    }
}
