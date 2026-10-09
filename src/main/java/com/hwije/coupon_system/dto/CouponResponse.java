package com.hwije.coupon_system.dto;

public class CouponResponse {

    private Long id;
    private String name;
    private Integer remainingQuantity;

    public CouponResponse(Long id, String name, Integer remainingQuantity) {
        this.id = id;
        this.name = name;
        this.remainingQuantity = remainingQuantity;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Integer getRemainingQuantity() {
        return remainingQuantity;
    }
}