package com.tt.Together_time.service;

import com.tt.Together_time.domain.dto.ProjectDto;
import com.tt.Together_time.domain.rdb.Member;
import com.tt.Together_time.domain.rdb.Project;
import com.tt.Together_time.domain.rdb.Team;
import com.tt.Together_time.repository.MemberRepository;
import com.tt.Together_time.repository.ProjectMongoRepository;
import com.tt.Together_time.repository.ProjectRepository;
import com.tt.Together_time.repository.TeamRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeamService {
    private final TeamRepository teamRepository;
    private final MemberRepository memberRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMongoRepository projectMongoRepository;
    private final ProjectDtoService projectDtoService;

    public List<ProjectDto> getProjects(String loggedEmail) {
        List<Project> projectList = teamRepository.findProjectsByMemberEmail(loggedEmail);

        return projectList.stream().map(projectDtoService::convertToDto).collect(Collectors.toList());
    }

    public void addTeam(String inviteMember, Long projectId) {
        Optional<Member> memberOptional = memberRepository.findMember(inviteMember);
        Optional<Project> projectOptional = projectRepository.findById(projectId);

        if(memberOptional.isEmpty())
            throw new EntityNotFoundException("Member with email " + inviteMember + " not found.");
        else if(projectOptional.isEmpty())
            throw new EntityNotFoundException("Project with projectId "+projectId+" not found.");


        teamRepository.save(
                Team.builder()
                        .member(memberOptional.get())
                        .project(projectOptional.get())
                        .build()
        );
    }

    public void addTeam(Member member, Project project) {
        //유효한 멤버인지 검증
        Optional<Member> memberOptional = memberRepository.findById(member.getId());
        teamRepository.save(
                Team.builder()
                    .member(memberOptional.get())
                    .project(project)
                    .build()
        );
    }

    public void removeTeam(Member member, Project project) {

    }

    public ResponseEntity<List<Member>> findByProjectId(Long projectId) {
        return teamRepository.findByProjectId(projectId);
    }
}
