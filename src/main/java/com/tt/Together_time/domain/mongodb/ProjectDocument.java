package com.tt.Together_time.domain.mongodb;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "projects")
@Getter
@Setter
@AllArgsConstructor
public class ProjectDocument {
    @Id
    private String id;

    private Long projectId;

    private List<String> tags;


}