package com.hwije.coupon_system.dto;

import java.time.LocalDateTime;

public class MyCouponResponse {

    private Long couponIssueId;
    private Long couponId;
    private String couponName;
    private LocalDateTime issuedAt;
    private LocalDateTime endAt;

    public MyCouponResponse(Long couponIssueId, Long couponId, String couponName,
                            LocalDateTime issuedAt, LocalDateTime endAt) {
        this.couponIssueId = couponIssueId;
        this.couponId = couponId;
        this.couponName = couponName;
        this.issuedAt = issuedAt;
        this.endAt = endAt;
    }

    public Long getCouponIssueId() {
        return couponIssueId;
    }

    public Long getCouponId() {
        return couponId;
    }

    public String getCouponName() {
        return couponName;
    }

    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }

    public LocalDateTime getEndAt() {
        return endAt;
    }
}
