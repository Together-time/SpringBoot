package com.tt.Together_time.controller;

import com.tt.Together_time.domain.dto.ScheduleDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/schedule")
public class ScheduleController {

    @GetMapping("/{projectId}")
    public ResponseEntity<List<ScheduleDto>> getSchedules(@PathVariable Long projectId, Authentication authentication){

        List<ScheduleDto> scheduleDtoList = new ArrayList<>();

        return ResponseEntity.ok().body(scheduleDtoList);
    }
}
