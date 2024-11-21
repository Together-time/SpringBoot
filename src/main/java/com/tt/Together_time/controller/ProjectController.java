package com.tt.Together_time.controller;

import com.tt.Together_time.domain.dto.MemberDto;
import com.tt.Together_time.domain.dto.ProjectCommand;
import com.tt.Together_time.domain.dto.ProjectDto;
import com.tt.Together_time.service.ProjectService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/projects")
public class ProjectController {
    private final AuthController authController;
    private final ProjectService projectService;

    //특정 프로젝트 정보 읽어오기
    @GetMapping("/{projectId}")
    public ResponseEntity<ProjectDto> getProject(@PathVariable Long projectId){
        try{
            ProjectDto projectDto = projectService.getProject(projectId);
            return ResponseEntity.ok().body(projectDto);
        } catch (EntityNotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    public ResponseEntity<Boolean> addProject(@RequestBody ProjectCommand projectCommand) {
        try {
            projectService.addProject(projectCommand);
            return ResponseEntity.ok().body(true);
        } catch (EntityNotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(false);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(false);
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
        }
    }

    @PutMapping("/{projectId}")
    public ResponseEntity<Boolean> updateProject(@PathVariable Long projectId, @RequestBody ProjectCommand projectCommand){
        try{
            projectService.updateProject(projectId, projectCommand.getTitle(), projectCommand.getTags());
            return ResponseEntity.ok().body(true);
        } catch (IllegalArgumentException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(false);
        } catch (EntityNotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(false);
        } catch(Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
        }
    }
    
    //태그 편집
    @PutMapping("/{projectId}/tag")
    public ResponseEntity<Boolean> updateProjectTags(@PathVariable Long projectId, @RequestParam List<String> tags){
        MemberDto loggedInMember = authController.getUserInfo().getBody();
        try{
            projectService.updateProjectTags(loggedInMember, projectId, tags);
            return ResponseEntity.ok(true);
        }catch (EntityNotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(false);
        }catch (AccessDeniedException e){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(false);
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
        }
    }

    @PatchMapping("/{projectId}/visibility")
    public ResponseEntity<Boolean> updateProjectStatus(@PathVariable Long projectId){
        MemberDto loggedInMember = authController.getUserInfo().getBody();

        try{
            projectService.updateProjectStatus(loggedInMember, projectId);
            return ResponseEntity.ok().body(true);
        } catch (EntityNotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(false);
        } catch (AccessDeniedException e){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(false);
        } catch(Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
        }
    }



    @DeleteMapping("{projectId}")
    public ResponseEntity<Boolean> removeProject(@PathVariable Long projectId){
        MemberDto loggedInMember = authController.getUserInfo().getBody();

        try{
            projectService.deleteById(loggedInMember, projectId);
            return ResponseEntity.ok().body(true);
        } catch (EntityNotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(false);
        } catch (AccessDeniedException e){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(false);
        } catch(Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
        }
    }
}
