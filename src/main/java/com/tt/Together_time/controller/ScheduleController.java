package com.tt.Together_time.controller;

import com.tt.Together_time.domain.dto.ScheduleRequest;
import com.tt.Together_time.service.MemberService;
import com.tt.Together_time.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/schedule/{projectId}")
public class ScheduleController {
    private final MemberService memberService;
    private final ScheduleService scheduleService;

    @GetMapping
    public ResponseEntity<List<ScheduleRequest>> getSchedules(@PathVariable Long projectId){
        List<ScheduleRequest> scheduleRequestList = scheduleService.findByProjectId(projectId);
        return ResponseEntity.ok().body(scheduleRequestList);
    }

    @PostMapping
    public ResponseEntity<Boolean> addSchedule(@PathVariable Long projectId, @RequestBody ScheduleRequest scheduleRequest){
        String loggedInMember = memberService.getUserEmail();

        scheduleService.addSchedule(loggedInMember, projectId, scheduleRequest);
        return ResponseEntity.ok(true);
    }

    @PutMapping
    public ResponseEntity<Boolean> updateSchedule(@PathVariable Long projectId, @RequestBody ScheduleRequest scheduleRequest){
        String loggedInMember = memberService.getUserEmail();

        scheduleService.updateSchedule(loggedInMember, projectId, scheduleRequest);
        return ResponseEntity.ok(true);
    }

    @DeleteMapping("/{scheduleId}")
    public ResponseEntity<Boolean> deleteSchedule(@PathVariable Long projectId, @PathVariable Long scheduleId){
        String loggedInMember = memberService.getUserEmail();

        scheduleService.deleteSchedule(loggedInMember, projectId, scheduleId);
        return ResponseEntity.ok(true);
    }
}
