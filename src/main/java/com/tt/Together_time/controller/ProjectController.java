package com.tt.Together_time.controller;

import com.tt.Together_time.domain.dto.ProjectCommand;
import com.tt.Together_time.domain.dto.ProjectDto;
import com.tt.Together_time.domain.enums.ProjectSortType;
import com.tt.Together_time.domain.mongodb.ProjectDocument;
import com.tt.Together_time.service.MemberService;
import com.tt.Together_time.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/projects")
public class ProjectController {
    private final ProjectService projectService;
    private final MemberService memberService;

    @GetMapping("/{projectId}")
    public ResponseEntity<ProjectDto> getProject(@PathVariable Long projectId){
        String loggedInMember = memberService.getUserEmail();
        ProjectDto projectDto = projectService.getProject(projectId, loggedInMember);
        return ResponseEntity.ok().body(projectDto);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProjectDocument>> searchProjects(@RequestParam String keyword, @RequestParam(required = false, defaultValue = "CREATED_DESC") ProjectSortType projectSortType){
        List<ProjectDocument> projectList = projectService.findProjectsByKeyword(keyword, projectSortType);
        return ResponseEntity.ok(projectList);
    }

    @PostMapping
    public ResponseEntity<Boolean> addProject(@RequestBody ProjectCommand projectCommand) {
        String loggedInMember = memberService.getUserEmail();

        projectService.addProject(projectCommand, loggedInMember);
        return ResponseEntity.ok().body(true);
    }

    @PutMapping("/{projectId}")
    public ResponseEntity<Boolean> updateProject(@PathVariable Long projectId, @RequestBody ProjectCommand projectCommand){
        projectService.updateProject(projectId, projectCommand);
        return ResponseEntity.ok().body(true);
    }
    
    //태그 편집
    @PutMapping("/{projectId}/tag")
    public ResponseEntity<Boolean> updateProjectTags(@PathVariable Long projectId, @RequestBody List<String> tags){
        String loggedInMember = memberService.getUserEmail();

        projectService.updateProjectTags(loggedInMember, projectId, tags);
        return ResponseEntity.ok(true);
    }

    @PatchMapping("/{projectId}/visibility")
    public ResponseEntity<Boolean> updateProjectStatus(@PathVariable Long projectId){
        String loggedInMember = memberService.getUserEmail();

        projectService.updateProjectStatus(loggedInMember, projectId);
        return ResponseEntity.ok().body(true);
    }



    @DeleteMapping("{projectId}")
    public ResponseEntity<Boolean> removeProject(@PathVariable Long projectId){
        String loggedInMember = memberService.getUserEmail();

        projectService.deleteById(loggedInMember, projectId);
        return ResponseEntity.ok().body(true);
    }
}
