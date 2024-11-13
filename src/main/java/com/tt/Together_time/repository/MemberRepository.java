package com.tt.Together_time.repository;

import com.tt.Together_time.domain.rdb.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    @Query("SELECT m FROM Member m WHERE m.email LIKE %:inviteMember% OR m.nickname LIKE %:inviteMember%")
    Optional<Member> findMember(String inviteMember);
}
