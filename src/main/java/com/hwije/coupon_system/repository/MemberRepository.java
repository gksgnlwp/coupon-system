package com.hwije.coupon_system.repository;

import com.hwije.coupon_system.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(String email);

    @Query("select m.email from Member m where m.email like concat(:prefix, '%')")
    List<String> findEmailsStartingWith(@Param("prefix") String prefix);

}