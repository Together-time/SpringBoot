package com.tt.Together_time.repository;

import com.tt.Together_time.domain.rdb.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    @Query("SELECT m FROM Member m WHERE m.email LIKE %:keyword% OR m.nickname LIKE %:keyword%")
    List<Member> findMember(String keyword);

    Optional<Member> findByEmail(String email);
}
