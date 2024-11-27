package com.tt.Together_time.repository;

import com.tt.Together_time.domain.rdb.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    @Query("SELECT s FROM Schedule s WHERE s.project.id=:projectId")
    List<Schedule> findByProjectId(Long projectId);
}
