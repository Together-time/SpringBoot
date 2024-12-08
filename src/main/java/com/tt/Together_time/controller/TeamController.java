package com.tt.Together_time.controller;

import com.tt.Together_time.domain.dto.MemberDto;
import com.tt.Together_time.domain.dto.ProjectDto;
import com.tt.Together_time.domain.rdb.Member;
import com.tt.Together_time.service.TeamService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/team")
public class TeamController {
    private final AuthController authController;
    private final TeamService teamService;

    //현재 로그인한 사용자가 참여 중인 프로젝트 리스트
    @GetMapping
    public ResponseEntity<List<ProjectDto>> getProjects(){
        String loggedMember = authController.getUserInfo().getBody();

        if(loggedMember!=null) {
            try {
                List<ProjectDto> projectDtoList = teamService.getProjects(loggedMember);
                return ResponseEntity.ok().body(projectDtoList);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        } else
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    
    //현재 프로젝트에 참여 중인 팀원 목록
    @GetMapping("/{projectId}")
    public ResponseEntity<List<MemberDto>> getMembers(@PathVariable Long projectId){
        try{
            List<MemberDto> memberDtos = teamService.findByProjectId(projectId);
            return ResponseEntity.ok(memberDtos);
        } catch (EntityNotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping    //팀원 추가
    public ResponseEntity<Boolean> addTeam(@RequestParam Member member, @RequestParam Long projectId){
        String loggedInMember = authController.getUserInfo().getBody();

        if(loggedInMember!=null){
            try {
                teamService.addTeam(loggedInMember, member, projectId);
                return ResponseEntity.ok(true);
            } catch (EntityNotFoundException e){
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(false);
            } catch (AccessDeniedException e){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(false);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(false);
            } catch (Exception e){
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
            }
        }else
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @DeleteMapping("/{projectId}")
    public ResponseEntity<Boolean> deleteTeam(@PathVariable Long projectId){
        String loggedInMember = authController.getUserInfo().getBody();

        if(loggedInMember!=null){
            try{
                teamService.leaveTeam(loggedInMember, projectId);
                return ResponseEntity.ok(true);
            } catch (EntityNotFoundException e){
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            } catch (Exception e){
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }else return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}
