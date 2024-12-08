package com.tt.Together_time.domain.rdb;

import com.tt.Together_time.domain.enums.ProjectVisibility;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;

    @CreatedDate
    private LocalDateTime createdAt;

    /*
    @LastModifiedDate
    private LocalDateTime lastEditedAt;
    */
    /*
    @OneToMany(mappedBy = "project")
    private List<Team> teamList = new ArrayList<>();

    @OneToMany(mappedBy = "project")
    private List<Message> messageList = new ArrayList<>();
    */
    private Long views;

    @Enumerated(EnumType.STRING)
    private ProjectVisibility status;
}
