package com.hwije.coupon_system.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private Integer quantity;

    private Integer remainingQuantity;

    private LocalDateTime startAt;

    private LocalDateTime endAt;

    private LocalDateTime createdAt;

    public Coupon() {
    }

    public void issue(LocalDateTime now) {

        if (startAt != null && now.isBefore(startAt)) {
            throw new IllegalStateException("아직 발급 기간이 아닙니다.");
        }

        if (endAt != null && now.isAfter(endAt)) {
            throw new IllegalStateException("발급 기간이 종료된 쿠폰입니다.");
        }

        if (remainingQuantity == null || remainingQuantity <= 0) {
            throw new IllegalStateException("쿠폰이 모두 소진되었습니다.");
        }

        remainingQuantity--;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public Integer getRemainingQuantity() {
        return remainingQuantity;
    }

    public LocalDateTime getStartAt() {
        return startAt;
    }

    public LocalDateTime getEndAt() {
        return endAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public void setRemainingQuantity(Integer remainingQuantity) {
        this.remainingQuantity = remainingQuantity;
    }

    public void setStartAt(LocalDateTime startAt) {
        this.startAt = startAt;
    }

    public void setEndAt(LocalDateTime endAt) {
        this.endAt = endAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}