package com.hwije.coupon_system.dto;

public class SignupResponse {

    private Long memberId;
    private String message;

    public SignupResponse(Long memberId, String message) {
        this.memberId = memberId;
        this.message = message;
    }

    public Long getMemberId() {
        return memberId;
    }

    public String getMessage() {
        return message;
    }
}