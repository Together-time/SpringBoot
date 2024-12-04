package com.tt.Together_time.controller;

import com.tt.Together_time.domain.dto.MemberDto;
import com.tt.Together_time.domain.dto.ScheduleRequest;
import com.tt.Together_time.service.ScheduleService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/schedule/{projectId}")
public class ScheduleController {
    private final AuthController authController;
    private final ScheduleService scheduleService;

    @GetMapping
    public ResponseEntity<List<ScheduleRequest>> getSchedules(@PathVariable Long projectId){
        try{
            List<ScheduleRequest> scheduleRequestList = scheduleService.findByProjectId(projectId);
            return ResponseEntity.ok().body(scheduleRequestList);
        }catch (EntityNotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    public ResponseEntity<Boolean> addSchedule(@PathVariable Long projectId, @RequestBody ScheduleRequest scheduleRequest){
        String loggedInMember = authController.getUserInfo().getBody();

        try{
            scheduleService.addSchedule(loggedInMember, projectId, scheduleRequest);
            return ResponseEntity.ok(true);
        }catch (EntityNotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(false);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(false);
        } catch(AccessDeniedException e){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(false);
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
        }
    }

    @PutMapping
    public ResponseEntity<Boolean> updateSchedule(@PathVariable Long projectId, @RequestBody ScheduleRequest scheduleRequest){
        String loggedInMember = authController.getUserInfo().getBody();

        try {
            scheduleService.updateSchedule(loggedInMember, projectId, scheduleRequest);
            return ResponseEntity.ok(true);
        }catch (EntityNotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(false);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(false);
        } catch(AccessDeniedException e){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(false);
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
        }
    }

    @DeleteMapping("/{scheduleId}")
    public ResponseEntity<Boolean> deleteSchedule(@PathVariable Long projectId, @PathVariable Long scheduleId){
        String loggedInMember = authController.getUserInfo().getBody();

        try {
            scheduleService.deleteSchedule(loggedInMember, projectId, scheduleId);
            return ResponseEntity.ok(true);
        }catch (EntityNotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(false);
        }catch(AccessDeniedException e){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(false);
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
        }
    }
}
