package com.hwije.coupon_system.dto;

public class CreateCouponResponse {

    private Long couponId;

    private String message;

    public CreateCouponResponse(Long couponId, String message) {
        this.couponId = couponId;
        this.message = message;
    }

    public Long getCouponId() {
        return couponId;
    }

    public String getMessage() {
        return message;
    }
}