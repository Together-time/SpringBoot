package com.tt.Together_time.controller;

import com.tt.Together_time.domain.dto.MemberDto;
import com.tt.Together_time.domain.dto.ProjectDto;
import com.tt.Together_time.domain.dto.TeamCommand;
import com.tt.Together_time.service.MemberService;
import com.tt.Together_time.service.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/team")
public class TeamController {
    private final MemberService memberService;
    private final TeamService teamService;

    //현재 로그인한 사용자가 참여 중인 프로젝트 리스트
    @GetMapping
    public ResponseEntity<List<ProjectDto>> getProjects(){
        String loggedMember = memberService.getUserEmail();

        List<ProjectDto> projectDtoList = teamService.getProjects(loggedMember);
        return ResponseEntity.ok().body(projectDtoList);
    }
    
    //현재 프로젝트에 참여 중인 팀원 목록
    @GetMapping("/{projectId}")
    public ResponseEntity<List<MemberDto>> getMembers(@PathVariable Long projectId){
        List<MemberDto> memberDtos = teamService.findByProjectId(projectId);
        return ResponseEntity.ok(memberDtos);
    }

    @PostMapping    //팀원 추가
    public ResponseEntity<Boolean> addTeam(@RequestBody TeamCommand teamCommand){
        String loggedInMember = memberService.getUserEmail();

        teamService.addTeam(loggedInMember, teamCommand.getMember(), teamCommand.getProjectId());
        return ResponseEntity.ok(true);
    }

    @DeleteMapping("/{projectId}")
    public ResponseEntity<Boolean> deleteTeam(@PathVariable Long projectId){
        String loggedInMember = memberService.getUserEmail();

        teamService.leaveTeam(loggedInMember, projectId);
        return ResponseEntity.ok(true);
    }
}
