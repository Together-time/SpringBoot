package com.tt.Together_time.controller;

import com.tt.Together_time.domain.dto.ProjectDto;
import com.tt.Together_time.domain.rdb.Member;
import com.tt.Together_time.service.MemberService;
import com.tt.Together_time.service.ProjectService;
import com.tt.Together_time.service.TeamService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/team/")
public class TeamController {
    private final TeamService teamService;
    private final MemberService memberService;
    private final ProjectService projectService;

    @GetMapping
    public ResponseEntity<List<ProjectDto>> getProjects(@AuthenticationPrincipal String loggedEmail){
        //현재 로그인한 사용자가 참여 중인 프로젝트 리스트
        //try-catch 구문 수정(추가)
        try{
            List<ProjectDto> projectDtoList = teamService.getProjects(loggedEmail);
            return ResponseEntity.ok().body(projectDtoList);
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    //현재 프로젝트에 참여 중인 팀원 목록
    @GetMapping("/{projectId}")
    public ResponseEntity<List<Member>> getMembers(@PathVariable Long projectId){
        try{
            return teamService.findByProjectId(projectId);
        } catch (EntityNotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    public ResponseEntity<Boolean> addTeam(@RequestParam String inviteMember, @RequestParam Long projectId){
        try {
            teamService.addTeam(inviteMember, projectId);
            return ResponseEntity.ok(true);
        } catch (EntityNotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(false);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(false);
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
        }
    }
    /*
    @DeleteMapping("/{projectId}/members/{memberId}")
    public ResponseEntity<Boolean> deleteTeam(@PathVariable Long projectId, @PathVariable Long memberId){
        Optional<Member> memberOptional = memberService.findById(memberId);
        Optional<Project> projectOptional = projectService.findById(projectId);

        if(memberOptional.isPresent()){
            teamService.removeTeam(memberOptional.get(), projectOptional.get());
        }else if(memberOptional.isEmpty())
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(false);

        return ResponseEntity.ok().body(true);
    }
    */
}
