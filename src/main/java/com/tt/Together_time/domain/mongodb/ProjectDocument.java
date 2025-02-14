package com.tt.Together_time.domain.mongodb;

import com.tt.Together_time.domain.enums.ProjectVisibility;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "projects")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProjectDocument {
    @Id
    private String id;

    private Long projectId;
    private String title;
    private ProjectVisibility status;
    private List<String> tags;
    private Long views;
    private LocalDateTime createdAt;
}
