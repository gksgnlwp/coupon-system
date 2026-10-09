package com.hwije.coupon_system.dto;

public class IssueCouponResponse {

    private Long couponIssueId;
    private String message;

    public IssueCouponResponse(Long couponIssueId, String message) {
        this.couponIssueId = couponIssueId;
        this.message = message;
    }

    public Long getCouponIssueId() {
        return couponIssueId;
    }

    public String getMessage() {
        return message;
    }
}
