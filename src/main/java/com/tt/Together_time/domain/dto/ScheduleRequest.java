package com.tt.Together_time.domain.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ScheduleRequest {
    private Long id;
    private String title;
    private String memo;
    private LocalDate startedDate;
    private LocalTime startedTime;
    private LocalDate endedDate;
    private LocalTime endedTime;
    private String color;
}
