package com.hwije.coupon_system.repository;

import com.hwije.coupon_system.entity.CouponIssue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CouponIssueRepository extends JpaRepository<CouponIssue, Long> {

    boolean existsByMemberIdAndCouponId(Long memberId, Long couponId);

    @Query("select ci from CouponIssue ci join fetch ci.coupon where ci.member.id = :memberId order by ci.issuedAt desc")
    List<CouponIssue> findAllByMemberIdWithCoupon(@Param("memberId") Long memberId);

    long countByCouponId(Long couponId);
}
