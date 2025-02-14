package com.tt.Together_time.domain.dto;

import com.tt.Together_time.domain.enums.ProjectVisibility;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProjectDto {
    private Long id;
    private String title;
    private ProjectVisibility status;
    private Long views;
    private List<String> tags;

    public ProjectDto(Long id, String title) {
        this.id = id;
        this.title = title;
    }
}