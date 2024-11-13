package com.tt.Together_time.controller;

import com.tt.Together_time.domain.dto.ProjectCommand;
import com.tt.Together_time.domain.dto.ProjectDto;
import com.tt.Together_time.service.ProjectService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/projects")
public class ProjectController {
    private final ProjectService projectService;

    //특정 프로젝트 정보 읽어오기
    @GetMapping
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

    @PutMapping
    public ResponseEntity<Boolean> updateProject(@RequestBody ProjectCommand projectCommand){

        return ResponseEntity.ok().body(true);
    }

    @DeleteMapping("{projectId}")
    public ResponseEntity<Boolean> removeProject(@PathVariable Long projectId){

        return ResponseEntity.ok().body(true);
    }
}
