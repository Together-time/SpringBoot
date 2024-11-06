package com.tt.Together_time.repository;

import com.tt.Together_time.domain.rdb.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);
}
