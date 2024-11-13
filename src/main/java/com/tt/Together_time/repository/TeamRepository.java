package com.tt.Together_time.repository;

import com.tt.Together_time.domain.rdb.Member;
import com.tt.Together_time.domain.rdb.Project;
import com.tt.Together_time.domain.rdb.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {

    @Query("SELECT t.project FROM Team t WHERE t.member.email = :email ORDER BY t.project.id DESC")
    List<Project> findProjectsByMemberEmail(@Param("email") String email);

    @Query("SELECT t.member FROM Team t WHERE t.project.id = :projectId")
    ResponseEntity<List<Member>> findByProjectId(Long projectId);
}
