package com.tt.Together_time.domain.rdb;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;
    private String nickname;
/*
    @OneToMany(mappedBy = "member")
    private List<Project> projectList = new ArrayList<>();

    @OneToMany(mappedBy = "member")
    private List<Team> teamList = new ArrayList<>();

    @OneToMany(mappedBy = "member")
    private List<Message> messageList = new ArrayList<>();
*/
}
