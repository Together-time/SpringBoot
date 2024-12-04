package com.tt.Together_time.service;

import com.tt.Together_time.domain.dto.MemberDto;
import com.tt.Together_time.domain.dto.ScheduleRequest;
import com.tt.Together_time.domain.rdb.Project;
import com.tt.Together_time.domain.rdb.Schedule;
import com.tt.Together_time.repository.ProjectRepository;
import com.tt.Together_time.repository.ScheduleRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScheduleService {
    private final ScheduleRepository scheduleRepository;
    private final ProjectRepository projectRepository;
    private final TeamService teamService;

    private ScheduleRequest convertToDto(Schedule schedule){
        return new ScheduleRequest(
                schedule.getId(),
                schedule.getTitle(),
                schedule.getMemo(),
                schedule.getStartedDate(),
                schedule.getStartedTime(),
                schedule.getEndedDate(),
                schedule.getEndedTime(),
                schedule.getColor()
        );
    }

    public List<ScheduleRequest> findByProjectId(Long projectId){
        Project project = projectRepository.findById(projectId)
                .orElseThrow(()-> new EntityNotFoundException());
        List<Schedule> scheduleList = scheduleRepository.findByProjectId(project.getId());

        return scheduleList.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    public void addSchedule(String logged, Long projectId, ScheduleRequest scheduleRequest){
        Project project = projectRepository.findById(projectId)
                .orElseThrow(()-> new EntityNotFoundException());

        boolean isExistingMember = teamService.existsByProjectIdAndMemberEmail(projectId, logged);

        if(isExistingMember){
            Schedule schedule = scheduleRepository.save(
                    Schedule.builder()
                            .project(project)
                            .title(scheduleRequest.getTitle())
                            .memo(scheduleRequest.getMemo())
                            .startedDate(scheduleRequest.getStartedDate())
                            .startedTime(scheduleRequest.getStartedTime())
                            .endedDate(scheduleRequest.getEndedDate())
                            .endedTime(scheduleRequest.getEndedTime())
                            .color(scheduleRequest.getColor())
                            .build()
            );
        }else
            throw new AccessDeniedException("권한이 없습니다.");
    }

    public void updateSchedule(String logged, Long projectId, ScheduleRequest scheduleRequest) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(()-> new EntityNotFoundException("프로젝트를 찾을 수 없습니다."));
        Schedule schedule = scheduleRepository.findById(scheduleRequest.getId())
                .orElseThrow(()->new EntityNotFoundException("일정을 찾을 수 없습니다."));

        boolean isExistingMember = teamService.existsByProjectIdAndMemberEmail(projectId, logged);

        if(isExistingMember){
            schedule.setTitle(scheduleRequest.getTitle());
            schedule.setMemo(scheduleRequest.getMemo());
            schedule.setStartedDate(scheduleRequest.getStartedDate());
            schedule.setStartedTime(scheduleRequest.getStartedTime());
            schedule.setEndedDate(scheduleRequest.getEndedDate());
            schedule.setEndedTime(scheduleRequest.getEndedTime());
            schedule.setColor(scheduleRequest.getColor());

            scheduleRepository.save(schedule);
        }else throw new AccessDeniedException("권한이 없습니다.");
    }

    public void deleteSchedule(String logged, Long projectId, Long scheduleId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(()-> new EntityNotFoundException("프로젝트를 찾을 수 없습니다."));
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(()->new EntityNotFoundException("일정을 찾을 수 없습니다."));

        boolean isExistingMember = teamService.existsByProjectIdAndMemberEmail(projectId, logged);

        if(isExistingMember) {
            scheduleRepository.deleteById(schedule.getId());
        }else throw new AccessDeniedException("권한이 없습니다.");
    }
}
