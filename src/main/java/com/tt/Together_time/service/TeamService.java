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

    public void addTeam(MemberDto logged, Member member, Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(()->new EntityNotFoundException());
        //권한 확인
        boolean isExistingMember = existsByProjectIdAndMemberEmail(projectId, logged.getEmail());
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

    @Transactional
    public void removeTeam(MemberDto memberDto, Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(()->new EntityNotFoundException());
        //권한 확인
        boolean isExistingMember = existsByProjectIdAndMemberEmail(projectId, memberDto.getEmail());

        if(isExistingMember){
            teamRepository.deleteMemberByEmail(memberDto.getEmail(), projectId);

            if(findByProjectId(projectId).size()==0)
                projectRepository.deleteById(projectId);
        }
    }

    public List<Member> findByProjectId(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(()->new EntityNotFoundException());

        List<Team> teamList = teamRepository.findByProjectId(project.getId());

        List<Member> members = teamList.stream().map(Team::getMember).collect(Collectors.toList());

        return members;
    }

    public void updateTeam(Project project, List<Member> members) {
        List<Member> originMembers = findByProjectId(project.getId());
        //팀원 내보내기 기능이 없으므로 추가만 하면 된다
        for(Member member : members){
            if(originMembers.contains(member))
                continue;
            addTeamByCreateProject(member, project);
        }
    }
}
