package com.tt.Together_time.repository;

import com.tt.Together_time.domain.enums.ProjectVisibility;
import com.tt.Together_time.domain.rdb.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    @Modifying
    @Query("UPDATE Project p SET p.title=:title WHERE p.id=:projectId")
    void updateProject(Long projectId, String title);

    @Modifying
    @Query("UPDATE Project p SET p.status = :visibility WHERE p.id = :projectId")
    void updateProjectStatus(Long projectId, ProjectVisibility visibility);
}
